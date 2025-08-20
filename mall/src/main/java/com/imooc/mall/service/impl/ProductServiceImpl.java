package com.imooc.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.mall.dao.ProductMapper;
import com.imooc.mall.enums.ProductDetailEnum;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.pojo.Product;
import com.imooc.mall.service.ICategoryService;
import com.imooc.mall.service.IProductService;
import com.imooc.mall.vo.ProductDetailVo;
import com.imooc.mall.vo.ProductVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 21:08
 */
@Service
@Slf4j
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private ProductMapper productMapper;
    @Override
    public ResponseVo<PageInfo> products(Integer categoryId, Integer pageNum, Integer pageSize) {
        HashSet<Integer> categoryIdSet = new HashSet<>();
        if (categoryId != null) {
            categoryService.findSubCategoryId(categoryId, categoryIdSet);
            categoryIdSet.add(categoryId);
        }
        /**
         * pageNUm第几页
         * pageSize每一页的数量
         * 先Size在Num
         */
        //Mybatis-PageHelper 分页插件
        PageHelper.startPage(pageNum, pageSize);

        List<Product> products = productMapper.selectByCategoryIdSet(categoryIdSet);
        log.info("products={}", products);
        List<ProductVo> productVoList = new ArrayList<>();
        for (Product product : products) {
            ProductVo productVo = new ProductVo();
            BeanUtils.copyProperties(product, productVo);
            productVoList.add(productVo);
        }

        PageInfo pageInfo = new PageInfo<>(products);
        pageInfo.setList(productVoList);

        log.info("productVoList={}", pageInfo);
        return ResponseVo.success(pageInfo);
    }

    @Override
    public ResponseVo<ProductDetailVo> productDetail(Integer productId) {
        Product product = productMapper.selectByPrimaryKey(productId);
        /**
         * if(!product.getStatus().equals(ProductDetailEnum.ON_SALE))
         *不推荐这种写法，万一以后加了其他的状态
         */
        if (product.getStatus().equals(ProductDetailEnum.OFF_SALE.getCode())
                || product.getStatus().equals(ProductDetailEnum.DELETE.getCode())) {
            return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE);
        }
        ProductDetailVo productDetailVo = new ProductDetailVo();
        BeanUtils.copyProperties(product, productDetailVo);
        return ResponseVo.success(productDetailVo);
    }
}
