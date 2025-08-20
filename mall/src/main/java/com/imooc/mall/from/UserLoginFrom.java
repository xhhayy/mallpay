package com.imooc.mall.from;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 0:21
 *
 * 登录表单
 */
@Data
public class UserLoginFrom {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
