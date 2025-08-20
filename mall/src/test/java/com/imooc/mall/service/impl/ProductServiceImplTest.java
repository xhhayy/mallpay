package com.imooc.mall.service.impl;

import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.service.IProductService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 21:31
 */
public class ProductServiceImplTest extends MallApplicationTests {
    @Autowired
    private IProductService productService;
    @Test
    public void products() {
        productService.products(null,2,2);
    }

    @Test
    public void productDetail() {
        productService.productDetail(26);
    }
}