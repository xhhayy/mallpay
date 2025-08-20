package com.imooc.mall.controller;

import com.imooc.mall.consts.MallConst;
import com.imooc.mall.from.CartAddFrom;
import com.imooc.mall.from.CartUpdateFrom;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
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
    ResponseVo<CartVo> list(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.list(user.getId());
    }
    /**
     * 购物车添加商品
     */
    @PostMapping("/carts")
    public ResponseVo<CartVo> cartAdd(@Valid @RequestBody CartAddFrom cartAddFrom,
                                      HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        log.info("进入购物车列表接口，用户ID：{}", user.getId()); // 添加日志
        return cartService.cartAdd(user.getId(), cartAddFrom);
    }
    /**
     * 更新购物车
     */
    @PostMapping("/carts/{productId}")
    ResponseVo<CartVo> update(@Valid @RequestBody CartUpdateFrom cartUpdateFrom,
                              @PathVariable Integer productId,
                              HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.update(user.getId(), productId, cartUpdateFrom);
    }
    /**
     * 移除购物车某个商品
     */
    @PostMapping("/carts/{productId}")
    ResponseVo<CartVo> delete(@PathVariable Integer productId,
                              HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.delete(user.getId(), productId);
    }
    /**
     * 全选中
     */
    @PostMapping("/carts/selectAll")
    ResponseVo<CartVo> selectAll(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.selectAll(user.getId());
    }
    /**
     * 全不选中
     */
    @PostMapping("/carts/unSelectAll")
    ResponseVo<CartVo> noSelectAll(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.noSelectAll(user.getId());
    }
    /**
     * 获取购物车全部商品总和
     */
    @GetMapping("/carts/products/sum")
    ResponseVo<Integer> sum(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.sum(user.getId());
    }
}
