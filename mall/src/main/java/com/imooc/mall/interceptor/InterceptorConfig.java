package com.imooc.mall.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 12:16
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Value("${file.upload.dir:D:/Prog/Java/Project/mallpay/mall/uploads/}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理员权限拦截器 - 拦截所有 /admin/** 路径
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**");
        
        // JWT拦截器 - 拦截普通用户接口
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/user/login", "/user/register", "/categories", "/products", "/products/*", "/admin/**", "/images/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /images/** 到本地文件系统
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
