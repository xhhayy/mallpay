package com.imooc.mall.from;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/24 16:45
 */
@Data
public class CartAddFrom {
    @NotNull
    private Integer productId;//商品id
    private Boolean selected = true;//是否选中

    public CartAddFrom(){

    }
    public CartAddFrom(Integer productId) {
        this.productId = productId;
    }
}
