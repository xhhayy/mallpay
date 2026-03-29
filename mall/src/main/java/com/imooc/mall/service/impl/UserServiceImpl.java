package com.imooc.mall.service.impl;

import com.imooc.mall.dao.UserMapper;
import com.imooc.mall.enums.RoleEnum;
import com.imooc.mall.pojo.User;
import com.imooc.mall.pojo.UserLoginInfo;
import com.imooc.mall.service.IUserService;
import com.imooc.mall.util.JwtUtil;
import com.imooc.mall.util.RedisUtil;
import com.imooc.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.imooc.mall.enums.ResponseEnum.*;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/22 18:57
 */
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 注册
     */
    @Override
    public ResponseVo<User> register(User user) {
        //user不能重复
        int countByUsername = userMapper.countByUsername(user.getUsername());
        if (countByUsername > 0) {
            return ResponseVo.error(USERNAME_EXIST);
        }
        //email不能重复
        int countByEmail = userMapper.countByEmail(user.getEmail());
        if (countByEmail > 0) {
            return ResponseVo.error(EMAIL_EXIST);
        }
        //密码MD5加密
        //MD5摘要算法（Spring自带）
        String s = DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8));
        user.setPassword(s);
        user.setRole(RoleEnum.CUSTOMER.getCode());//设置为普通用户
        //写入数据库
        int resultCount = userMapper.insertSelective(user);
        if (resultCount == 0) {
            return ResponseVo.error(ERROR);
        }
        return ResponseVo.success();
    }
    /**
     * 登录并生成Token，存储到Redis
     */
    @Override
    public ResponseVo<Map<String, Object>> login(String username, String password) {
        // 账号密码校验
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            //用户不存在(返回：用户名或密码错误)
            return ResponseVo.error(USERNAME_OR_PASSWORD_ERROR);
        }
        String s = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!s.equalsIgnoreCase(user.getPassword())) {
            //密码错误(返回：用户名或密码错误)
            return ResponseVo.error(USERNAME_OR_PASSWORD_ERROR);
        }
        user.setPassword("");
        
        // 生成 Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 准备用户权限信息
        List<String> permissions = new ArrayList<>();
        if (user.getRole() == RoleEnum.ADMIN.getCode()) {
            // 管理员权限
            permissions.add("admin:manage");
            permissions.add("admin:category:manage");
            permissions.add("admin:product:manage");
            permissions.add("admin:order:manage");
            permissions.add("admin:user:manage");
        } else {
            // 普通用户权限
            permissions.add("user:cart:manage");
            permissions.add("user:order:manage");
            permissions.add("user:address:manage");
        }
        
        // 计算过期时间
        long expireTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000; // 24小时
        
        // 创建用户登录信息
        UserLoginInfo loginInfo = new UserLoginInfo(
                user.getId(),
                user.getUsername(),
                token,
                user.getRole(),
                permissions,
                expireTime
        );
        
        // 存储到Redis
        String userKey = RedisUtil.generateUserKey(user.getId());
        redisUtil.set(userKey, loginInfo, 24 * 60 * 60); // 24小时过期
        
        // 返回 Token 和用户信息
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return ResponseVo.success(data);
    }

    /**
     * 返回用户总数
     * @return
     */
    @Override
    public int countAll() {
        return userMapper.countAll();
    }
}
