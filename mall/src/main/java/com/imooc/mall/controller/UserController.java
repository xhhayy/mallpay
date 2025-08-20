package com.imooc.mall.controller;

import com.imooc.mall.consts.MallConst;
import com.imooc.mall.from.UserLoginFrom;
import com.imooc.mall.from.UserRegisterFrom;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IUserService;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
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
    /**
     *注册
     */
    @PostMapping("/user/register")
    //表单验证
    //注册
    public ResponseVo<User> register(@Valid @RequestBody UserRegisterFrom userFrom){
//        if(bindingResult.hasErrors()){
//            log.error("注册提交的参数有误,{}{}" ,
//                    bindingResult.getFieldError().getField(),
//                    bindingResult.getFieldError().getDefaultMessage() );
//            //getField()获取的是UserFrom中为空的属性，etDefaultMessage()获取的是为空属性的描述
//        return ResponseVo.error(PARAM_ERROR,bindingResult);
//        }
        User user = new User();
        BeanUtils.copyProperties(userFrom,user);//拷贝对象:BeanUtils.copyProperties(原对象，对象)
        return userService.register(user);
    }
    /**
     *登录
     */
    @PostMapping("/user/login")
    public ResponseVo<User> login(@Valid @RequestBody UserLoginFrom userLoginFrom,
                                  HttpSession session){
//        if(bindingResult.hasErrors()){
//            return ResponseVo.error(PARAM_ERROR,bindingResult);
//        }
        ResponseVo<User> userResponseVo = userService.login(userLoginFrom.getUsername(), userLoginFrom.getPassword());
        //设置Session
        session.setAttribute(MallConst.CURRENT_USER,userResponseVo.getData());
        return userResponseVo;
    }
    /**
     *获取登用户录信息
     * session保存在内存中  改进版：token+redis     token-->sessionId
     */
    @GetMapping("/user")
    public ResponseVo user(HttpSession session){
        //由拦截器进行第一次判断
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return ResponseVo.success(user);
    }
    /**
     *退出登录
     */
    @PostMapping("/user/logout")
    public ResponseVo logout(HttpSession session){
        //由拦截器进行第一次判断
        session.removeAttribute(MallConst.CURRENT_USER);
        return ResponseVo.success();
    }
}
