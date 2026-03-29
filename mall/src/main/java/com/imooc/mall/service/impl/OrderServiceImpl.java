package com.imooc.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.mall.dao.OrderItemMapper;
import com.imooc.mall.dao.OrderMapper;
import com.imooc.mall.dao.ProductMapper;
import com.imooc.mall.dao.ShippingMapper;
import com.imooc.mall.enums.OrderStatusEnum;
import com.imooc.mall.enums.PaymentTypeEnum;
import com.imooc.mall.enums.ProductDetailEnum;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.form.CartAddForm;
import com.imooc.mall.pojo.*;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.service.IOrderService;
import com.imooc.mall.vo.OrderItemVo;
import com.imooc.mall.vo.OrderVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/25 14:49
 */
@Service
@Slf4j
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ICartService cartService;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    // 分布式锁前缀
    private static final String LOCK_PRODUCT = "product_lock:";
    // 锁过期时间（毫秒）
    private static final long LOCK_EXPIRE_TIME = 10 * 1000;
    // 看门狗续期时间（毫秒）
    private static final long WATCHDOG_INTERVAL = 3 * 1000;
    // 线程池，用于执行看门狗任务
    private final ScheduledExecutorService watchdogExecutorService = Executors.newScheduledThreadPool(5);
    // 延迟队列
    @Autowired
    private OrderDelayService orderDelayService;

    @Override
    @Transactional //事务   出现错误就不会写进数据库
    public ResponseVo<OrderVo> create(Integer uid, Integer shippingId) {
        //收货地址校验（总之要查出来的）
        Shipping shipping = shippingMapper.selectByUidAndShippingId(uid, shippingId);
        if (shipping == null) {
            return ResponseVo.error(ResponseEnum.SHIPPING_NOT_EXIST);
        }

        //获取购物车的商品，校验（是否有商品、库存）
        List<Cart> cartList = cartService.listForCart(uid).stream()
                .filter(Cart::getProductSelected)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(cartList)) {
            return ResponseVo.error(ResponseEnum.CART_SELECTED_IS_EMPTY);
        }
        //获取cartList里的productIds
        Set<Integer> productIdSet = cartList.stream()
                .map(Cart::getProductId)
                .collect(Collectors.toSet());
        //把所有商品存入map(productId,product)
        List<Product> productList = productMapper.selectByProductIdSet(productIdSet);
        Map<Integer, Product> map  = productList.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<OrderItem> orderItemList = new ArrayList<>();
        //创建订单编号
        Long orderNo = generateOrderNo();
        // 假设订单超时时间为 30 分钟（30 * 60 * 1000 毫秒）
        long delayTime = 1 * 60 * 1000;
        //将订单添加到延迟队列中，超时未支付自动取消
        orderDelayService.addOrderToDelayQueue(orderNo, delayTime);

        for (Cart cart : cartList) {
            //根据productId查数据库
            Product product = map.get(cart.getProductId());
            log.info("product={}"+product);
            if (product == null) {
                return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST, "商品不存在.productId=" + cart.getProductId());
            }
            // 判断商品是否下架
            if (!ProductDetailEnum.ON_SALE.getCode().equals(product.getStatus())) {
                return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE, "该商品不是在售状态" + product.getName());
            }
            
            // 商品ID
            Integer productId = product.getId();
            // 分布式锁key
            String lockKey = LOCK_PRODUCT + productId;
            // 线程唯一标识
            String lockValue = UUID.randomUUID().toString();
            // 锁状态
            AtomicBoolean lockAcquired = new AtomicBoolean(false);
            // 看门狗运行状态
            AtomicBoolean watchdogRunning = new AtomicBoolean(false);
            // 看门狗任务
            ScheduledFuture<?> watchdogTask = null;
            
            try {
                // 1. 尝试获取分布式锁
                // 使用SET NX EX命令，确保原子性和自动过期
                Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
                if (!lock) {
                    return ResponseVo.error(ResponseEnum.PROODUCT_STOCK_ERROR, "商品库存操作繁忙，请稍后重试" + productId);
                }
                lockAcquired.set(true);
                
                // 2. 启动看门狗线程，定期续期锁
                watchdogRunning.set(true);
                watchdogTask = watchdogExecutorService.scheduleWithFixedDelay(() -> {
                    if (watchdogRunning.get()) {
                        try {
                            // 检查锁是否存在且属于当前线程
                            String currentValue = (String) redisTemplate.opsForValue().get(lockKey);
                            if (lockValue.equals(currentValue)) {
                                // 续期锁
                                redisTemplate.expire(lockKey, LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
                                log.info("看门狗为商品 {} 的锁续期成功", productId);
                            } else {
                                // 锁已被释放或被其他线程获取，停止看门狗
                                watchdogRunning.set(false);
                                log.info("商品 {} 的锁已被释放或变更，停止看门狗", productId);
                            }
                        } catch (Exception e) {
                            log.error("看门狗续期失败", e);
                            watchdogRunning.set(false);
                        }
                    }
                }, WATCHDOG_INTERVAL, WATCHDOG_INTERVAL, TimeUnit.MILLISECONDS);
                
                // 3. 执行业务逻辑
                // 重新查询库存，避免缓存过期导致的问题
                Product latestProduct = productMapper.selectByPrimaryKey(productId);
                if (latestProduct == null) {
                    return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST, "商品不存在.productId=" + productId);
                }
                // 检查库存是否充足
                if (latestProduct.getStock() < cart.getQuantity()) {
                    return ResponseVo.error(ResponseEnum.PROODUCT_STOCK_ERROR, "库存不足" + latestProduct.getName());
                }
                
                // 4. 构建OrderItem对象
                OrderItem orderItem = buildOrderItem(uid, orderNo, cart.getQuantity(), latestProduct);
                orderItemList.add(orderItem);

                // 5. 减库存
                latestProduct.setStock(latestProduct.getStock() - cart.getQuantity());
                int i = productMapper.updateByPrimaryKeySelective(latestProduct);
                if (i <= 0) {
                    return ResponseVo.error(ResponseEnum.ERROR);
                }
                log.info("商品 {} 库存扣减成功，剩余库存: {}", productId, latestProduct.getStock());
                
            } finally {
                // 6. 停止看门狗
                if (watchdogTask != null) {
                    watchdogRunning.set(false);
                    watchdogTask.cancel(true);
                }
                
                // 7. 释放分布式锁（使用Lua脚本保证原子性）
                if (lockAcquired.get()) {
                    releaseLock(lockKey, lockValue);
                }
            }
        }

        //计算总价，只计算选中的
        //生成订单，入库：Order和OrderItem 使用事务，两个都成功才提交事务
        //构建Order对象
        Order order = buildOrder(uid, orderNo, shippingId, orderItemList);
        //写入数据库
        int i = orderMapper.insertSelective(order);
        if(i <= 0){
            return ResponseVo.error(ResponseEnum.ERROR);
        }
        int j = orderItemMapper.batchInsert(orderItemList);
        if(j <= 0){
            return ResponseVo.error(ResponseEnum.ERROR);
        }
        //更新购物车（选中的商品）
        for (Cart cart : cartList) {
            cartService.delete(uid,cart.getProductId());//删除已经购买的商品
        }
        OrderVo orderVo = buildOrderVo(order, orderItemList, shipping);
        log.info("orderVo={}"+orderVo);
        //构造OrderVO，返回前端
        return ResponseVo.success(orderVo);
    }

    @Override//2.订单列表
    public ResponseVo<PageInfo> list(Integer uid, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUid(uid);

        Set<Long> orderNoSet = orderList.stream()
                .map(Order::getOrderNo)
                .collect(Collectors.toSet());
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoSet(orderNoSet);
        Map<Long, List<OrderItem>> orderItemMap = orderItemList.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderNo));

        Set<Integer> shippingIdSet = orderList.stream()
                .map(Order::getShippingId)
                .collect(Collectors.toSet());
        List<Shipping> shippingList = shippingMapper.selectByIdSet(shippingIdSet);
        Map<Integer, Shipping> shippingMap = shippingList.stream()
                .collect(Collectors.toMap(Shipping::getId, shipping -> shipping));
        //获取orderVoList
        List<OrderVo> orderVoList = new ArrayList<>();
        for (Order order : orderList) {
            OrderVo orderVo = buildOrderVo(order,
                    orderItemMap.get(order.getOrderNo()),
                    shippingMap.get(order.getShippingId()));
            orderVoList.add(orderVo);
        }
        PageInfo pageInfo = new PageInfo<>(orderList);
        pageInfo.setList(orderVoList);

        return ResponseVo.success(pageInfo);
    }

    @Override//3，订单详情
    public ResponseVo<OrderVo> detail(Integer uid, Long orderNo) {
        //查询order
        Order order = orderMapper.selectByOrderNo(orderNo);
        //查询orderItem
        Set<Long> orderNoSet = new HashSet<>();
        orderNoSet.add(order.getOrderNo());
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoSet(orderNoSet);
        //获取地址
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        OrderVo orderVo = buildOrderVo(order, orderItemList, shipping);
        return ResponseVo.success(orderVo);
    }

    @Override//4.取消订单
    @Transactional // 添加事务注解，确保数据一致性
    public ResponseVo cancel(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null || !order.getUserId().equals(uid)){
            return ResponseVo.error(ResponseEnum.ORDER_NOT_EXIST);
        }
        //只有未付款才能取消，看自己公司业务
        if(!order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())){
            return ResponseVo.error(ResponseEnum.ORDER_STATUS_ERROR);
        }
        
        // 获取订单项
        Set<Long> orderNoSet = new HashSet<>();
        orderNoSet.add(orderNo);
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoSet(orderNoSet);
        
        // 恢复库存（在MySQL事务中）
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + orderItem.getQuantity());
                productMapper.updateByPrimaryKeySelective(product);
            }
        }
        
        // 更新订单状态（在MySQL事务中）
        order.setStatus(OrderStatusEnum.CANCELED.getCode());
        order.setUpdateTime(new Date());
        int i = orderMapper.updateByPrimaryKeySelective(order);
        if(i <= 0){
            return ResponseVo.error(ResponseEnum.PASSWORD_ERROR,"该用户没有此订单");
        }
        
        // MySQL事务提交后，再操作Redis购物车（异步处理，即使失败也不影响订单取消）
        try {
            for (OrderItem orderItem : orderItemList) {
                // 将商品添加回购物车
                for (int j = 0; j < orderItem.getQuantity(); j++) {
                    CartAddForm cartAddForm = new CartAddForm(orderItem.getProductId());
                    cartAddForm.setSelected(false); // 设置为未选中状态
                    cartService.cartAdd(uid, cartAddForm);
                }
            }
        } catch (Exception e) {
            // Redis操作失败不影响订单取消，只记录日志
            log.error("订单取消后商品回退购物车失败，orderNo={}", orderNo, e);
        }
        
        return ResponseVo.success();
    }
    @Override//修改订单状态
    public void paid(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException(ResponseEnum.ORDER_NOT_EXIST.getDesc() + "订单id:" + orderNo);
        }
        //只有[未付款]订单可以变成[已付款]，看自己公司业务
        if (!order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())) {
            throw new RuntimeException(ResponseEnum.ORDER_STATUS_ERROR.getDesc() + "订单id:" + orderNo);
        }

        order.setStatus(OrderStatusEnum.PAID.getCode());
        order.setPaymentTime(new Date());
        int row = orderMapper.updateByPrimaryKeySelective(order);
        if (row <= 0) {
            throw new RuntimeException("将订单更新为已支付状态失败，订单id:" + orderNo);
        }
    }

    private Order buildOrder(Integer uid,
                             Long orderNo,
                             Integer shippingId,
                             List<OrderItem> orderItemList){
        BigDecimal payment = orderItemList.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);//计算总价

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(uid);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(PaymentTypeEnum.PAY_ONLINE.getCode());
        order.setPostage(0);
        order.setStatus(OrderStatusEnum.NO_PAY.getCode());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        return order;
    }

    /**
     * 企业级：分布式唯一id/主键
     * @return
     */
    private Long generateOrderNo() {//使用时间戳+随机数产生订单号
        return System.currentTimeMillis() + new Random().nextInt(999);
    }
    //构建orderItem对象
    private OrderItem buildOrderItem(Integer uid,Long orderNo,Integer quantity,Product product){
        OrderItem item = new OrderItem();
        item.setUserId(uid);
        item.setOrderNo(orderNo);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getMainImage());
        item.setCurrentUnitPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
    //构建OrderVo对象
    private OrderVo buildOrderVo(Order order,List<OrderItem> orderItemList,Shipping shipping){
        OrderVo orderVo = new OrderVo();
        BeanUtils.copyProperties(order,orderVo);

        List<OrderItemVo> orderItemVoList = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = new OrderItemVo();
            BeanUtils.copyProperties(orderItem,orderItemVo);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        if(shipping != null){
            orderVo.setShippingId(shipping.getId());
            orderVo.setShippingVo(shipping);
        }
        return orderVo;
    }

    @Override
    @Transactional // 添加事务注解，确保数据一致性
    public void cancelOrder(Long orderId) {
        // 实现取消订单的逻辑
        Order order = orderMapper.selectByOrderNo(orderId);
        if (order == null) {
            return;
        }
        //订单超时还未付款才取消
        if(order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())){
            // 获取订单项
            Set<Long> orderNoSet = new HashSet<>();
            orderNoSet.add(orderId);
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoSet(orderNoSet);
            
            // 恢复库存（在MySQL事务中）
            for (OrderItem orderItem : orderItemList) {
                Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + orderItem.getQuantity());
                    productMapper.updateByPrimaryKeySelective(product);
                }
            }
            
            //把状态改为已取消（在MySQL事务中）
            order.setStatus(OrderStatusEnum.CANCELED.getCode());
            order.setUpdateTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            
            // MySQL事务提交后，再操作Redis购物车（异步处理，即使失败也不影响订单取消）
            try {
                for (OrderItem orderItem : orderItemList) {
                    // 将商品添加回购物车
                    for (int i = 0; i < orderItem.getQuantity(); i++) {
                        CartAddForm cartAddForm = new CartAddForm(orderItem.getProductId());
                        cartAddForm.setSelected(false); // 设置为未选中状态
                        cartService.cartAdd(order.getUserId(), cartAddForm);
                    }
                }
            } catch (Exception e) {
                // Redis操作失败不影响订单取消，只记录日志
                log.error("订单超时自动取消后商品回退购物车失败，orderNo={}", orderId, e);
            }
        }
    }

    @Override
    public int countAll() {
        return orderMapper.countAll();
    }

    @Override
    public BigDecimal sumTotalPayment() {
        return orderMapper.sumTotalPayment();
    }

    /**
     * 释放分布式锁（使用Lua脚本保证原子性）
     * @param lockKey 锁的key
     * @param lockValue 锁的value（线程唯一标识）
     */
    private void releaseLock(String lockKey, String lockValue) {
        // Lua脚本：检查锁是否存在且属于当前线程，是则删除
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        
        try {
            Long result = (Long) redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
            if (result != null && result > 0) {
                log.info("释放锁 {} 成功", lockKey);
            } else {
                log.info("释放锁 {} 失败，锁不存在或不属于当前线程", lockKey);
            }
        } catch (Exception e) {
            log.error("释放锁失败", e);
        }
    }
}
