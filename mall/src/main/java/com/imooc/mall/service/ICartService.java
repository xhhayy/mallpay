package com.imooc.mall.service;

import com.imooc.mall.form.CartAddForm;
import com.imooc.mall.form.CartUpdateForm;
import com.imooc.mall.pojo.Cart;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;

import java.util.List;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/24 17:43
 */

public interface ICartService {

    ResponseVo<CartVo> cartAdd(Integer uid,CartAddForm cartAddForm);//添加购物车

    ResponseVo<CartVo> list(Integer uid);

    ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateForm cartUpdateForm);//更新购物车

    ResponseVo<CartVo> delete(Integer uid, Integer productId);//从购物车中移除某个商品

    ResponseVo<CartVo> selectAll(Integer uid);//选购物车中所有的商品

    ResponseVo<CartVo> noSelectAll(Integer uid);//全部不选中

    ResponseVo<Integer> sum(Integer uid);//购物车总数量

    public List<Cart> listForCart(Integer uid);//从redis中取出数据
}
