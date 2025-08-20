package com.imooc.mall.service.impl;

import com.google.gson.Gson;
import com.imooc.mall.dao.ProductMapper;
import com.imooc.mall.enums.ProductDetailEnum;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.from.CartAddFrom;
import com.imooc.mall.from.CartUpdateFrom;
import com.imooc.mall.pojo.Cart;
import com.imooc.mall.pojo.Product;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.vo.CartProductVo;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/24 17:46
 */
@SuppressWarnings("all")
@Service
@Slf4j
public class CartServiceImpl implements ICartService {

    private final static String CART_REDIS_KEY_TEMPLATE = "cart_%d";
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate redisTemplates;//redis封装的对象

    private Gson gson = new Gson();//把数据变成json类型
    /**
     *添加购物车
     */
    @Override
    public ResponseVo<CartVo> cartAdd(Integer uid, CartAddFrom cArtAddFrom) {
        Integer quantity = 1;//商品添加进购物车的数量默认为1

        // 商品是否存在
        Product product = productMapper.selectByPrimaryKey(cArtAddFrom.getProductId());
        if (product == null) {
            return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST);
        }

        // 商品是否正常在售
        if (!product.getStatus().equals(ProductDetailEnum.ON_SALE.getCode())) {
            return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE);
        }

        // 商品库存是否充足
        if (product.getStock() <= 0) {
            return ResponseVo.error(ResponseEnum.PROODUCT_STOCK_ERROR);
        }

        // 写入redis
        // Key:cart_userId
        // 用的redis里面的Hash数据类型-->(cart_userId,productId,cart)
        HashOperations<String, String, String> opsForHash = redisTemplates.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        Cart cart;
        // 从redis中读取购物车，判断购物车是否有该商品
        String value = opsForHash.get(redisKey, String.valueOf(product.getId()));
        if (StringUtil.isNullOrEmpty(value)) {//是否为空
            //没有该商品
            cart = new Cart(product.getId(), quantity, cArtAddFrom.getSelected());
        } else {
            //有该商品，数量+1
            cart = gson.fromJson(value, Cart.class);//json格式-->cart对象
            cart.setQuantity(cart.getQuantity() + quantity);
        }

        opsForHash.put(redisKey,
                String.valueOf(product.getId()),
                gson.toJson(cart));

        return list(uid);
    }
    /**
     *购物车列表
     */
    @Override
    public ResponseVo<CartVo> list(Integer uid) {
        //创建redis的一个Hash的对象。
        HashOperations<String, String, String> opsForHash = redisTemplates.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        Map<String, String> entries = opsForHash.entries(redisKey);

        CartVo cartVo = new CartVo();
        //设置初始值
        Boolean selectedAll = true;//全部选中
        BigDecimal cartTotalPrice = BigDecimal.ZERO;//总价格
        Integer cartTotalQuantity = 0;//总数量
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Integer productId = Integer.valueOf(entry.getKey());
            Cart cart = gson.fromJson(entry.getValue(), Cart.class);

            //TODO 需要优化，用mysql里的in

            Product product = productMapper.selectByPrimaryKey(productId);

            if (product != null) {
                CartProductVo cartProductVo = new CartProductVo(product.getId(), cart.getQuantity(),
                        product.getName(), product.getSubtitle(),
                        product.getMainImage(), product.getPrice(),
                        product.getStatus(), product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())),
                        product.getStock(), cart.getProductSelected());
                cartProductVoList.add(cartProductVo);

                if (!cart.getProductSelected()) {
                    selectedAll = false;
                }
                //只计算选中的
                if (cart.getProductSelected()) {
                    cartTotalPrice = cartTotalPrice.add(cartProductVo.getProductTotalPrice());
                }
            }
            cartTotalQuantity += cart.getQuantity();
        }
        cartVo.setCartProductVoList(cartProductVoList);
        //有一个没有选中，就不是全选
        cartVo.setSelectedAll(selectedAll);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartTotalQuantity(cartTotalQuantity);
        return ResponseVo.success(cartVo);
    }
    /**
     *更新购物车
     */
    @Override
    public ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateFrom from) {
        //把redis里面的数据读取出来
        HashOperations<String, String, String> opsForHash = redisTemplates.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        String value = opsForHash.get(redisKey, String.valueOf(productId));
        if (StringUtil.isNullOrEmpty(value)) {//是否为空
            //没有该商品    报错
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }

        Cart cart = gson.fromJson(value, Cart.class);
        //有该商品，修改数量,判断传进进来的参数不为空才修改
        if (from.getQuantity() != null && from.getQuantity() > 0) {
            cart.setQuantity(from.getQuantity());
        }
        if (from.getSelected() != null) {
            cart.setProductSelected(from.getSelected());
        }
        //把修改后的数据重新写入redis
        opsForHash.put(redisKey,
                String.valueOf(productId),
                gson.toJson(cart));
        return list(uid);
    }

    @Override//从购物车中移除某个商品
    public ResponseVo<CartVo> delete(Integer uid, Integer productId) {
        HashOperations<String, String, String> opsForHash = redisTemplates.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        String value = opsForHash.get(redisKey, String.valueOf(productId));
        if (StringUtil.isNullOrEmpty(value)) {//是否为空
            //没有该商品    报错
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }

        opsForHash.delete(redisKey, String.valueOf(productId));//从redis删除该商品

        return list(uid);
    }

    @Override//选购物车中所有的商品
    public ResponseVo<CartVo> selectAll(Integer uid) {
        //把redis中的商品遍历，设置selected属性
        HashOperations<String, String, String> opsForHash = redisTemplates.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        Map<String, String> entries = opsForHash.entries(redisKey);
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Cart cart = gson.fromJson(entry.getValue(), Cart.class);
            cart.setProductSelected(true);
            opsForHash.put(redisKey,String.valueOf(cart.getProductId()),gson.toJson(cart));
        }

        return list(uid);
    }

    @Override//全部不选中
    public ResponseVo<CartVo> noSelectAll(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplates.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        Map<String, String> entries = opsForHash.entries(redisKey);
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Cart cart = gson.fromJson(entry.getValue(), Cart.class);
            cart.setProductSelected(false);
            opsForHash.put(redisKey,String.valueOf(cart.getProductId()),gson.toJson(cart));
        }
        return list(uid);
    }

    @Override//购物车总数量
    public ResponseVo<Integer> sum(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplates.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        Integer sum = 0;
        Map<String, String> entries = opsForHash.entries(redisKey);
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Cart cart = gson.fromJson(entry.getValue(), Cart.class);
            sum += cart.getQuantity();
        }
        return ResponseVo.success(sum);
    }

    public List<Cart> listForCart(Integer uid) {
        //从redis把该用户的购物车取出
        HashOperations<String, String, String> opsForHash = redisTemplates.opsForHash();
        String redisKey  = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        Map<String, String> entries = opsForHash.entries(redisKey);//购物车的全部数据

        List<Cart> cartList = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {//转换成一个个Cart对象，存入list集合中
            cartList.add(gson.fromJson(entry.getValue(), Cart.class));
        }
        log.info("cartList={}"+cartList);
        return cartList;
    }

}
