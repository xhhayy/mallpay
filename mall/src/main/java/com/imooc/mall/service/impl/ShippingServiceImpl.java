package com.imooc.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.mall.dao.ShippingMapper;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.from.ShippingFrom;
import com.imooc.mall.pojo.Shipping;
import com.imooc.mall.service.IShippingService;
import com.imooc.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/25 11:35
 */
@Service
public class ShippingServiceImpl implements IShippingService {
    @Autowired
    private ShippingMapper shippingMapper;

    @Override//添加地址
    public ResponseVo<Map<String,Integer>> addShipping(Integer uid, ShippingFrom shippingFrom) {
        Shipping shipping = new Shipping();
        BeanUtils.copyProperties(shippingFrom,shipping);
        shipping.setUserId(uid);
        int insert = shippingMapper.insertSelective(shipping);

        if (insert == 0){
            return ResponseVo.error(ResponseEnum.PASSWORD_ERROR,"地址新建失败");
        }

        Map<String, Integer> map = new HashMap<>();
        map.put("shippingId",shipping.getId());
        return ResponseVo.success("地址新建成功",map);
    }

    @Override//删除地址
    public ResponseVo delete(Integer uid, Integer shippingId) {
        int i = shippingMapper.deleteByIdAndUid(uid, shippingId);
        if(i == 0){
            return ResponseVo.error(ResponseEnum.PASSWORD_ERROR,"删除地址失败");
        }

        return ResponseVo.success("删除地址成功");
    }

    @Override//更新地址
    public ResponseVo update(Integer uid, Integer shippingId, ShippingFrom shippingFrom) {
        Shipping shipping = shippingMapper.selectByUidAndShippingId(uid, shippingId);
        BeanUtils.copyProperties(shippingFrom,shipping);

        int i = shippingMapper.updateByPrimaryKeySelective(shipping);
        if(i == 0){
            ResponseVo.error(ResponseEnum.PASSWORD_ERROR,"地址更新失败");
        }
        return ResponseVo.success("地址更新成功");
    }

    @Override//查看所有地址
    public ResponseVo<PageInfo> selectAll(Integer uid,Integer pageNum, Integer pageSize) {
        /**
         * pageSize每一页的数量
         * pageNUm第几页
         * 先Size在Num
         */
        //Mybatis-PageHelper 分页插件
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippings = shippingMapper.selectByUid(uid);

        PageInfo pageInfo = new PageInfo<>(shippings);

        return ResponseVo.success(pageInfo);
    }


}
