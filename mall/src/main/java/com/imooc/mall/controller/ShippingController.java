package com.imooc.mall.controller;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.consts.MallConst;
import com.imooc.mall.from.ShippingFrom;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IShippingService;
import com.imooc.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Map;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/25 13:39
 */
@RestController
public class ShippingController {
    @Autowired
    private IShippingService shippingService;
    /**
     *添加地址
     */
    @PostMapping("/shippings")
    public ResponseVo<Map<String, Integer>> addShipping(@Valid @RequestBody ShippingFrom shippingFrom,
                                                        HttpSession session){
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return shippingService.addShipping(user.getId(),shippingFrom);
    }
    /**
     *删除地址
     */
    @DeleteMapping("/shippings/{shippingId}")
    public ResponseVo delete(@PathVariable String shippingId,HttpSession session){
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return shippingService.delete(user.getId(), Integer.valueOf(shippingId));
    }
    /**
     *修改地址
     */
    @PutMapping("/shippings/{shippingId}")
    public ResponseVo updete(@PathVariable String shippingId,
                             @Valid @RequestBody ShippingFrom shippingFrom,
                             HttpSession session){
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return shippingService.update(user.getId(), Integer.valueOf(shippingId),shippingFrom);
    }
    /**
     *查看所有地址
     */
    @GetMapping("/shippings")
    public ResponseVo<PageInfo> selectAll(@RequestParam(required = false,defaultValue = "1") Integer pageNum,
                                          @RequestParam(required = false,defaultValue = "10") Integer pageSize,
                                          HttpSession session){
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return shippingService.selectAll(user.getId(),pageNum,pageSize);
    }
}
