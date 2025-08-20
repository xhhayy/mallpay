package com.imooc.mall.service.impl;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.service.IOrderService;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/25 17:47
 */
@Slf4j
public class OrderServiceTest extends MallApplicationTests {

    @Autowired
    private IOrderService iOrderService;
    @Test
    public void create() {
        iOrderService.create(1,17);
    }

    @Test
    public void list() {
        ResponseVo<PageInfo> list = iOrderService.list(1, 1, 10);
        log.info("list={}"+list);
    }

//    @Test
//    public void detail() {
//        ResponseVo<OrderVo> detail = iOrderService.detail(1, 42905079203L);
//        log.info("detail={}"+detail);
//    }
}