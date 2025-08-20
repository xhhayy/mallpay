package com.imooc.mall.controller;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.service.IProductService;
import com.imooc.mall.vo.ProductDetailVo;
import com.imooc.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 22:45
 */
@RestController
public class ProductController {
    @Autowired
    private IProductService productService;
    /**
     * 全部商品
     */
    @GetMapping("/products")
    public ResponseVo<PageInfo> products(@RequestParam(required = false) Integer categoryId,
                                         @RequestParam(required = false,defaultValue = "1") Integer pageNum,
                                         @RequestParam(required = false,defaultValue = "10") Integer pageSize){
        return productService.products(categoryId,pageNum,pageSize);
    }
    /**
     * 商品详细
     */
    @GetMapping("/products/{productId}")//{}表示在url中用@PathVariable
    public ResponseVo<ProductDetailVo> productDetail(@PathVariable Integer productId){
        return productService.productDetail(productId);
    }
}
