package com.imooc.mall.from;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/22 21:45
 */
@Data
public class UserRegisterFrom {
    /**
     * @NotBlank  用于 String 判断空格
     * @NonNull
     * @NotEmpty 用于集合
     *
     * 注册表单
     */
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String email;
}
