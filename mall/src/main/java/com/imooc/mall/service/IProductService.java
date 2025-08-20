package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.vo.ProductDetailVo;
import com.imooc.mall.vo.ResponseVo;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 21:04
 */
public interface IProductService {
    ResponseVo<PageInfo> products(Integer categoryId, Integer pageNum, Integer pageSize);//全部商品

    ResponseVo<ProductDetailVo> productDetail(Integer productId);//商品详细
}
