package com.imooc.mall.controller;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.consts.MallConst;
import com.imooc.mall.from.OrderCreateFrom;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IOrderService;
import com.imooc.mall.vo.OrderVo;
import com.imooc.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
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
    @PostMapping("orders")
    public ResponseVo<OrderVo> create(@Valid @RequestBody OrderCreateFrom from,
                                      HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return orderService.create(user.getId(), from.getShippingId());
    }
    /**
     *订单列表
     */
    @GetMapping("/orders")
    public ResponseVo<PageInfo> list(@RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                     @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                     HttpSession session) {
        User user = (User)session.getAttribute(MallConst.CURRENT_USER);
        return orderService.list(user.getId(),pageNum,pageSize);
    }
    /**
     *订单详细
     */
    @GetMapping("/orders/{orderNo}")
    public ResponseVo<OrderVo> detail(@PathVariable Long orderNo,
                                      HttpSession session){
        User user = (User)session.getAttribute(MallConst.CURRENT_USER);
        return orderService.detail(user.getId(), orderNo);
    }
    /**
     *取消订单
     */
    @PutMapping("/order/{orderNo}")
    public ResponseVo cancel(@PathVariable Long orderNo,
                             HttpSession session){
        User user = (User)session.getAttribute(MallConst.CURRENT_USER);
        return orderService.cancel(user.getId(), orderNo);
    }
}
