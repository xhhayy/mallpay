package com.imooc.mall.controller;

import com.imooc.mall.service.ICategoryService;
import com.imooc.mall.vo.CategoryVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 14:53
 */
@RestController
@Slf4j
public class CategoryController {
    @Autowired
    private ICategoryService categoryService;
    /**
     *商品类别
     */
    @GetMapping("/categories")
    public ResponseVo<List<CategoryVo>> selectAll() {
        ResponseVo<List<CategoryVo>> listResponseVo = categoryService.selectAll();
        log.info("到前端啦。。。。。。。。。。。。。");
        log.info("listResponseVo{}",listResponseVo);
        return listResponseVo;
    }
}
