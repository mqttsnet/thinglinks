package com.mqttsnet.thinglinks.common.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 作用到方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
/**
 * @功能描述 防止重复提交标记注解
 * @author thinglinks
 * @date 2022-02-15
 */
public @interface NoRepeatSubmit {
}
