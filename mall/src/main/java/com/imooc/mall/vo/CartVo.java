package com.imooc.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/24 16:30
 */
@Data
public class CartVo {
    private List<CartProductVo> cartProductVoList;
    private Boolean selectedAll;//是否全选
    private BigDecimal cartTotalPrice;//购物车总价
    private Integer cartTotalQuantity;//购物车总的数量
}
