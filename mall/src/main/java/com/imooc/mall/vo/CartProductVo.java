package com.imooc.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/24 16:30
 */
@Data
public class CartProductVo {
    private Integer parentId;

    private Integer quantity;//数量

    private String productName;

    private String productSubtitle;//商品子标题

    private String productMainImage;

    private BigDecimal productPrice;

    private Integer productStatus;

    private BigDecimal productTotalPrice;//商品总价=quantity*productPrice

    private Integer productStock;//商品库存

    private Boolean productSelected;//是否选中该商品

    public CartProductVo() {
    }

    public CartProductVo(Integer parentId, Integer quantity, String productName, String productSubtitle, String productMainImage, BigDecimal productPrice, Integer productStatus, BigDecimal productTotalPrice, Integer productStock, Boolean productSelected) {
        this.parentId = parentId;
        this.quantity = quantity;
        this.productName = productName;
        this.productSubtitle = productSubtitle;
        this.productMainImage = productMainImage;
        this.productPrice = productPrice;
        this.productStatus = productStatus;
        this.productTotalPrice = productTotalPrice;
        this.productStock = productStock;
        this.productSelected = productSelected;
    }
}
