package com.easymusic.aspect;

import com.easymusic.annotation.GlobalInterceptor;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.enums.ResponseCodeEnum;
import com.easymusic.exception.BusinessException;
import com.easymusic.redis.RedisComponent;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * @author cyt
 * * @date 2025/11/21 12:46:38
 */

@Aspect // 不属于spring
@Component("globalOperationAspect") // 可以不加名称
@Slf4j
public class GlobalOperationAspect {

    @Resource
    private RedisComponent redisComponent;

    @Before("@annotation(com.easymusic.annotation.GlobalInterceptor)")
    public void intercept(JoinPoint point) throws Throwable {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
        log.info("进入切面 GlobalInterceptor: " + interceptor);
        if (interceptor == null) {
            return;
        }

        if (interceptor.checkLogin()) {
            checkLogin();
        }

    }

    private void checkLogin() {
        // 从请求头中获取token的字段
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        // 没有携带token 直接返回null
        if (StringTools.isEmpty(token)) {
            throw new BusinessException(ResponseCodeEnum.CODE_901); // 用户未登录
        }
        // 从redis中获取用户信息
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getUserTokenInfoDto(token);
        if (tokenUserInfoDTO == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
    }

}
