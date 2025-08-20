package com.imooc.mall.from;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/26 11:03
 */
@Data
public class OrderCreateFrom {
    @NotNull
    private Integer shippingId;
}
