package com.imooc.mall.from;

import lombok.Data;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/24 21:53
 */
@Data
public class CartUpdateFrom {
    private Integer quantity;
    private Boolean selected;
}
