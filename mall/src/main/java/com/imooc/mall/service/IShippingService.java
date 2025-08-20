package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.from.ShippingFrom;
import com.imooc.mall.vo.ResponseVo;

import java.util.Map;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/25 11:32
 */
public interface IShippingService {


    ResponseVo<Map<String,Integer>> addShipping(Integer uid, ShippingFrom shippingFrom);//添加地址

    ResponseVo delete(Integer uid,Integer shippingId);//删除地址

    ResponseVo update(Integer uid,Integer shippingId,ShippingFrom shippingFrom);//更新地址
    ResponseVo<PageInfo> selectAll(Integer uid, Integer pageNum, Integer pageSize);//查看所有地址


}
