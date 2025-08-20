package com.imooc.mall.enums;

import lombok.Getter;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/22 19:41
 *
 * 角色0-管理员,1-普通用户
 */
@Getter
public enum RoleEnum {
        ADMIN(0),//管理员
        CUSTOMER(1),//普通用户
;
        Integer code;

    RoleEnum(Integer code) {
        this.code = code;
    }
}
