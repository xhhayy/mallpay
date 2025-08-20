package com.imooc.mall.service.impl;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.from.ShippingFrom;
import com.imooc.mall.service.IShippingService;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/25 12:04
 */
@Slf4j
public class ShippingServiceImplTest extends MallApplicationTests {

    @Autowired
    private IShippingService shippingService;
    @Test
    public void selectAll() {
        ResponseVo<PageInfo> shippingList = shippingService.selectAll(1, 1, 10);
        log.info("shippingList={}"+shippingList);
    }

    @Test
    public void addShipping() {
        ShippingFrom shippingFrom = new ShippingFrom();
        shippingFrom.setReceiverName("徐伟翔");
        shippingFrom.setReceiverPhone("020");
        shippingFrom.setReceiverMobile("18322862912");
        shippingFrom.setReceiverProvince("江西");
        shippingFrom.setReceiverCity("江西省");
        shippingFrom.setReceiverDistrict("南昌市");
        shippingFrom.setReceiverAddress("乐平村");
        shippingFrom.setReceiverZip("333300");
        ResponseVo<Map<String, Integer>> list = shippingService.addShipping(1, shippingFrom);
        log.info("list={}"+list);

    }

    @Test
    public void delete() {
        ResponseVo delete = shippingService.delete(1, 15);
    }

    @Test
    public void update() {
        ShippingFrom shippingFrom = new ShippingFrom();
        shippingFrom.setReceiverName("许思雨");
        shippingFrom.setReceiverPhone("020");
        shippingFrom.setReceiverMobile("18322862912");
        shippingFrom.setReceiverProvince("江西");
        shippingFrom.setReceiverCity("江西省");
        shippingFrom.setReceiverDistrict("南昌市");
        shippingFrom.setReceiverAddress("乐平村");
        shippingFrom.setReceiverZip("333300");
        ResponseVo update = shippingService.update(1, 16, shippingFrom);
        log.info("list={}"+update);

    }
}