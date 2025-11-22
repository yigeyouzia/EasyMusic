package com.easymusic.interceptor;

import com.easymusic.entity.dto.TokenUserInfo4AdminDTO;
import com.easymusic.entity.enums.ResponseCodeEnum;
import com.easymusic.exception.BusinessException;
import com.easymusic.redis.RedisComponent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author cyt
 * * @date 2025/11/22 17:21:57
 */
@Component
public class AppInterceptor implements HandlerInterceptor {

    private final String URL_ACCOUNT = "/account";

    private final String URL_FILE = "/file";

    @Resource
    private final RedisComponent redisComponent;

    public AppInterceptor(RedisComponent redisComponent) {
        this.redisComponent = redisComponent;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler == null) {
            return false;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        return checkLogin(request);
    }

    private Boolean checkLogin(HttpServletRequest request) {
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        StringBuffer url = request.getRequestURL();
        // 判断是否是登录页面
        if (url.indexOf(URL_ACCOUNT) != -1 || url.indexOf(URL_FILE) != -1) {
            return true;
        }
        // 判断是否登录
        String token = request.getHeader("token");
        TokenUserInfo4AdminDTO tokenUserInfo4Admin = redisComponent.getTokenUserInfo4Admin(token);
        if (tokenUserInfo4Admin == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        return true;
    }
}
