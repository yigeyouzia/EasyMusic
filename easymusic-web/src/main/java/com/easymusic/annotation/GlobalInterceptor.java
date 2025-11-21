package com.easymusic.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * @author cyt
 * * @date 2025/11/21 12:55:16
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME) // 注解会在运行时被JVM读取
@Documented // 说明该注解可以被javadoc工具记录 可以不加
@Mapping // 该注解可以被Spring MVC的注解处理器处理  可以不加
public @interface GlobalInterceptor {

    boolean checkLogin() default false; // 是否检查登录

}
