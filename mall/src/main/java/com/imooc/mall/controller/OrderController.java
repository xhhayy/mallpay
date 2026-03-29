package com.imooc.mall.controller;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.form.OrderCreateForm;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IOrderService;
import com.imooc.mall.util.UserThreadLocal;
import com.imooc.mall.vo.OrderVo;
import com.imooc.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/26 10:44
 */
@RestController
public class OrderController {
    @Autowired
    private IOrderService orderService;
    /**
     *创建订单
     */
    @PostMapping("/orders")
    public ResponseVo<OrderVo> create(@Valid @RequestBody OrderCreateForm form) {
        User user = UserThreadLocal.getUser();
        return orderService.create(user.getId(), form.getShippingId());
    }
    /**
     *订单列表
     */
    @GetMapping("/orders")
    public ResponseVo<PageInfo> list(@RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                     @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        User user = UserThreadLocal.getUser();
        return orderService.list(user.getId(),pageNum,pageSize);
    }
    /**
     *订单详细
     */
    @GetMapping("/orders/{orderNo}")
    public ResponseVo<OrderVo> detail(@PathVariable Long orderNo){
        User user = UserThreadLocal.getUser();
        return orderService.detail(user.getId(), orderNo);
    }
    /**
     *取消订单
     */
    @PutMapping("/order/{orderNo}")
    public ResponseVo cancel(@PathVariable Long orderNo){
        User user = UserThreadLocal.getUser();
        return orderService.cancel(user.getId(), orderNo);
    }
}
