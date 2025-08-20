package com.imooc.mall.vo;

import lombok.Data;

import java.util.List;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 14:15
 */
@Data
public class CategoryVo {
    private Integer id;

    private Integer parentId;

    private String name;

    private Integer sortOrder;

    private List<CategoryVo> subCategories;   //递归

}
