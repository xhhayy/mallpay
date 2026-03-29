package com.imooc.mall.controller;

import com.imooc.mall.form.CartAddForm;
import com.imooc.mall.form.CartUpdateForm;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.util.UserThreadLocal;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/24 16:49
 */
@RestController
@Slf4j
public class CartController {
    @Autowired
    private ICartService cartService;
    /**
     * 购物车列表
     */
    @GetMapping("/carts")
    ResponseVo<CartVo> list() {
        User user = UserThreadLocal.getUser();
        return cartService.list(user.getId());
    }
    /**
     * 购物车添加商品
     */
    @PostMapping("/carts")
    public ResponseVo<CartVo> cartAdd(@Valid @RequestBody CartAddForm cartAddForm) {
        User user = UserThreadLocal.getUser();
        log.info("进入购物车列表接口，用户ID：{}", user.getId()); // 添加日志
        return cartService.cartAdd(user.getId(), cartAddForm);
    }
    /**
     * 更新购物车
     */
    @PutMapping("/carts/{productId}")
    ResponseVo<CartVo> update(@Valid @RequestBody CartUpdateForm cartUpdateForm,
                              @PathVariable Integer productId) {
        User user = UserThreadLocal.getUser();
        return cartService.update(user.getId(), productId, cartUpdateForm);
    }
    /**
     * 移除购物车某个商品
     */
    @DeleteMapping("/carts/{productId}")
    ResponseVo<CartVo> delete(@PathVariable Integer productId) {
        User user = UserThreadLocal.getUser();
        return cartService.delete(user.getId(), productId);
    }
    /**
     * 全选中
     */
    @PostMapping("/carts/selectAll")
    ResponseVo<CartVo> selectAll() {
        User user = UserThreadLocal.getUser();
        return cartService.selectAll(user.getId());
    }
    /**
     * 全不选中
     */
    @PostMapping("/carts/unSelectAll")
    ResponseVo<CartVo> noSelectAll() {
        User user = UserThreadLocal.getUser();
        return cartService.noSelectAll(user.getId());
    }
    /**
     * 获取购物车全部商品总和
     */
    @GetMapping("/carts/products/sum")
    ResponseVo<Integer> sum() {
        User user = UserThreadLocal.getUser();
        return cartService.sum(user.getId());
    }
}
