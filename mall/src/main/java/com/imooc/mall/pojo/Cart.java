package com.imooc.mall.pojo;

import lombok.Data;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/24 18:18
 */
@Data
public class Cart {
    private Integer productId;//商品id
    private Integer quantity;//数量
    private Boolean productSelected;//是否选中该商品

    public Cart() {
    }

    public Cart(Integer productId, Integer quantity, Boolean productSelected) {
        this.productId = productId;
        this.quantity = quantity;
        this.productSelected = productSelected;
    }
}
