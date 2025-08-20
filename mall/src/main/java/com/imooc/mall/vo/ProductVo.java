package com.imooc.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 21:01
 */
@Data
public class ProductVo {
    private Integer id;

    private Integer categoryId;

    private String name;

    private String subtitle;
    private String mainImage;
    private Integer status;
    private BigDecimal price;

}
