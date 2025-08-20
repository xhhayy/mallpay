package com.imooc.mall.enums;

import lombok.Getter;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/25 18:35
 */
@Getter
public enum PaymentTypeEnum {
    PAY_ONLINE(1),//在线支付
            ;

    Integer code;

    PaymentTypeEnum(Integer code) {
        this.code = code;
    }
}
