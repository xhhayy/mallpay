package com.imooc.mall.service.impl;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.form.ShippingForm;
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
        ShippingForm shippingForm = new ShippingForm();
        shippingForm.setReceiverName("徐伟翔");
        shippingForm.setReceiverPhone("18322862912");
        shippingForm.setReceiverProvince("江西");
        shippingForm.setReceiverCity("江西省");
        shippingForm.setReceiverDistrict("南昌市");
        shippingForm.setReceiverAddress("乐平村");
        shippingForm.setReceiverZip("333300");
        ResponseVo<Map<String, Integer>> list = shippingService.addShipping(1, shippingForm);
        log.info("list={}"+list);

    }

    @Test
    public void delete() {
        ResponseVo delete = shippingService.delete(1, 15);
    }

    @Test
    public void update() {
        ShippingForm shippingForm = new ShippingForm();
        shippingForm.setReceiverName("许思雨");
        shippingForm.setReceiverPhone("18322862912");
        shippingForm.setReceiverProvince("江西");
        shippingForm.setReceiverCity("江西省");
        shippingForm.setReceiverDistrict("南昌市");
        shippingForm.setReceiverAddress("乐平村");
        shippingForm.setReceiverZip("333300");
        ResponseVo update = shippingService.update(1, 16, shippingForm);
        log.info("list={}"+update);

    }
}