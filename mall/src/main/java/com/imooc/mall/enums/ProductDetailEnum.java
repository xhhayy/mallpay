package com.imooc.mall.enums;

import lombok.Getter;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/24 0:06
 */
@Getter
public enum ProductDetailEnum {
    ON_SALE(1),
    OFF_SALE(2),
    DELETE(3),
    ;

    Integer code;

    public Integer getCode() {
        return code;
    }

    ProductDetailEnum(Integer code) {
        this.code = code;
    }
}
