package com.imooc.mall.controller;

import com.imooc.mall.form.UserLoginForm;
import com.imooc.mall.form.UserRegisterForm;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IUserService;
import com.imooc.mall.util.RedisUtil;
import com.imooc.mall.util.UserThreadLocal;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/22 21:05
 */
@RestController
@Slf4j
public class UserController {
    @Autowired
    private IUserService userService;
    
    @Autowired
    private RedisUtil redisUtil;
    /**
     *注册
     */
    @PostMapping("/user/register")
    //表单验证
    //注册
    public ResponseVo<User> register(@Valid @RequestBody UserRegisterForm userForm){
//        if(bindingResult.hasErrors()){
//            log.error("注册提交的参数有误,{}{}" ,
//                    bindingResult.getFieldError().getField(),
//                    bindingResult.getFieldError().getDefaultMessage() );
//            //getField()获取的是UserFrom中为空的属性，etDefaultMessage()获取的是为空属性的描述
//        return ResponseVo.error(PARAM_ERROR,bindingResult);
//        }
        User user = new User();
        BeanUtils.copyProperties(userForm,user);//拷贝对象:BeanUtils.copyProperties(原对象，对象)
        return userService.register(user);
    }
    /**
     *登录
     */
    @PostMapping("/user/login")
    public ResponseVo login(@Valid @RequestBody UserLoginForm userLoginForm){
        return userService.login(userLoginForm.getUsername(), userLoginForm.getPassword());
    }

    /**
     *获取登用户录信息
     */
    @GetMapping("/user")
    public ResponseVo user(){
        // 从ThreadLocal中获取用户信息
        User user = UserThreadLocal.getUser();
        return ResponseVo.success(user);
    }

    /**
     *退出登录
     */
    @PostMapping("/user/logout")
    public ResponseVo logout(){
        // 从ThreadLocal中获取用户信息
        User user = UserThreadLocal.getUser();
        if (user != null) {
            // 从Redis中删除登录信息
            String userKey = RedisUtil.generateUserKey(user.getId());
            redisUtil.delete(userKey);
        }
        return ResponseVo.success();
    }
}
