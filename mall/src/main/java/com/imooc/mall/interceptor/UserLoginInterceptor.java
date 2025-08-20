package com.imooc.mall.interceptor;

import com.imooc.mall.exception.UserLoginException;
import com.imooc.mall.consts.MallConst;
import com.imooc.mall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 12:01
 * 拦截器
 */
@Slf4j
@Component
public class UserLoginInterceptor implements HandlerInterceptor {
    /**
     * true流程继续，false表示中断
     */
    @Override
    //执行之前拦截
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("进去preHandle...");
        User user = (User) request.getSession().getAttribute(MallConst.CURRENT_USER);
        log.info(".....................");
        if (user == null) {
            log.info("user==null");
              throw new UserLoginException();
//            return false;
        }
        log.info("没进入if中");
        return true;
    }
}
