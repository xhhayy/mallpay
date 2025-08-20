package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.vo.OrderVo;
import com.imooc.mall.vo.ResponseVo;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/25 14:47
 */
public interface IOrderService {

    ResponseVo<OrderVo> create(Integer uid,Integer shippingId);//添加订单

    ResponseVo<PageInfo> list(Integer uid,Integer pageNum, Integer pageSize);//订单列表

    ResponseVo<OrderVo> detail(Integer uid,Long orderNo);//订单详情

    ResponseVo cancel(Integer uid,Long orderNo);//取消订单

    void paid(Long orderNo);//修改订单状态

    void cancelOrder(Long orderId);//取消订单
}
