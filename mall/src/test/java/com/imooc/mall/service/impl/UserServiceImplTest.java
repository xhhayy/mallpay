package com.imooc.mall.service.impl;

import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.enums.RoleEnum;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IUserService;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/22 19:28
 */
//事务，单测只测功能，不要对数据造成污染
@Slf4j
public class UserServiceImplTest extends MallApplicationTests {
   @Autowired
    private IUserService userService;

    @Test     //@Before 表示这个方法是前置方法
    public void register() {
        User user = new User("xhh","123456","xhh@qq.com", RoleEnum.CUSTOMER.getCode());
        userService.register(user);
    }

    @Test
    public void login() {
        ResponseVo<User> responseVo = userService.login("Tom","123456" );
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }
}