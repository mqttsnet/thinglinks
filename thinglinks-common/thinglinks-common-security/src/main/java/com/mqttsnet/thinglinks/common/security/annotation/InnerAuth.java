package com.mqttsnet.thinglinks.common.security.annotation;

import java.lang.annotation.*;

/**
 * 内部认证注解
 * 
 * @author thinglinks
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InnerAuth
{
    /**
     * 是否校验用户信息
     */
    boolean isUser() default false;
}