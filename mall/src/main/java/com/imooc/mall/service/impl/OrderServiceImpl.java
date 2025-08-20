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
import com.imooc.mall.pojo.*;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.service.IOrderService;
import com.imooc.mall.vo.OrderItemVo;
import com.imooc.mall.vo.OrderVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private static final String LOCK_PRODUCT = "product_lock:";
    //延迟队列
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
            //判断商品是否下架
            if (!ProductDetailEnum.ON_SALE.getCode().equals(product.getStatus())) {
                return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE, "该商品不是在售状态" + product.getName());
            }
            //库存是否充足
            if (product.getStock() < cart.getQuantity()) {
                return ResponseVo.error(ResponseEnum.PROODUCT_STOCK_ERROR, "库存不正确" + product.getName());
            }
            //一次只允许一个线程操作库存
            String lockKey = LOCK_PRODUCT + product.getId();
            try{
                boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey + product.getId()
                        ,"lock",
                        10, TimeUnit.SECONDS);
                if(!lock){
                    return ResponseVo.error(ResponseEnum.PROODUCT_STOCK_ERROR,"商品库存操作繁忙，请稍后重试"+product.getId());
                }
                //构建OrderItem对象
                OrderItem orderItem = buildOrderItem(uid, orderNo, cart.getQuantity(), product);
                orderItemList.add(orderItem);

                //减库存
                product.setStock(product.getStock()- cart.getQuantity());
                int i = productMapper.updateByPrimaryKeySelective(product);//保存回数据库
                if(i <= 0){
                    return ResponseVo.error(ResponseEnum.ERROR);
                }
            }finally {
                redisTemplate.delete(lockKey);
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
    public ResponseVo cancel(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null || !order.getUserId().equals(uid)){
            return ResponseVo.error(ResponseEnum.ORDER_NOT_EXIST);
        }
        //只有未付款才能取消，看自己公司业务
        if(!order.getStatus().equals(OrderStatusEnum.NO_PAY)){
            return ResponseVo.error(ResponseEnum.ORDER_STATUS_ERROR);
        }
        order.setStatus(OrderStatusEnum.CANCELED.getCode());
        order.setUpdateTime(new Date());
        int i = orderMapper.updateByPrimaryKeySelective(order);
        if(i <= 0){
            return ResponseVo.error(ResponseEnum.PASSWORD_ERROR,"该用户没有此订单");
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
    public void cancelOrder(Long orderId) {
        // 实现取消订单的逻辑
        Order order = orderMapper.selectByOrderNo(orderId);
        //订单超时还未付款才取消
        if(order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())){
            //把状态改为已取消
            order.setStatus(OrderStatusEnum.CANCELED.getCode());
            order.setUpdateTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }
    }
}
