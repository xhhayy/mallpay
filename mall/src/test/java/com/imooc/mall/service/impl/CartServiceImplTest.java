package com.imooc.mall.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.from.CartAddFrom;
import com.imooc.mall.from.CartUpdateFrom;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/24 18:33
 */
@Slf4j
public class CartServiceImplTest extends MallApplicationTests {
    @Autowired
    private ICartService cartService;

    @Autowired
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Test
    public void cartAdd() {
        ResponseVo<CartVo> list = cartService.cartAdd(1, new CartAddFrom(27));
        log.info("list={}", gson.toJson(list));
    }

    @Test
    public void list() {
        ResponseVo<CartVo> list = cartService.list(1);
        log.info("list={}", gson.toJson(list));
//        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

    @Test
    public void update() {
        CartUpdateFrom cartUpdateFrom = new CartUpdateFrom();
        cartUpdateFrom.setQuantity(10);
        cartUpdateFrom.setSelected(false);
        ResponseVo<CartVo> list = cartService.update(1,26,cartUpdateFrom);
        log.info("list={}", gson.toJson(list));
    }

    @Test
    public void delete() {

        ResponseVo<CartVo> list = cartService.delete(1,29);
        log.info("list={}", gson.toJson(list));
    }

    @Test
    public void selectAll() {
        ResponseVo<CartVo> list = cartService.selectAll(1);
        log.info("list={}", gson.toJson(list));
    }

    @Test
    public void noSelectAll() {
        ResponseVo<CartVo> list = cartService.noSelectAll(1);
        log.info("list={}", gson.toJson(list));
    }

    @Test
    public void sum() {
        ResponseVo<Integer> sum = cartService.sum(1);

        log.info("list={}",sum);
    }
}