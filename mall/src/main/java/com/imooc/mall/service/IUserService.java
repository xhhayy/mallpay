package com.imooc.mall.service;

import com.imooc.mall.pojo.User;
import com.imooc.mall.vo.ResponseVo;

import java.util.Map;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/22 18:56
 */
public interface IUserService {
    /**
     * 注册账号
     * username
     * password
     * email
     */
    ResponseVo<User> register(User user);
    /**
     *登录并生成Token，存储到Redis
     */
    ResponseVo<Map<String, Object>> login(String username, String password);

    int countAll();
}
