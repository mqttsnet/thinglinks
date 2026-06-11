package com.mqttsnet.thinglinks.bus.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;

/**
 * Topic 路由声明注解,标在 adapter 实现类上声明处理哪些 topic + 业务分组。
 * 替代旧的 if/else 链:Registry 启动扫描自动建索引。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TopicRoute {

    /**
     * 匹配的 topic 列表(支持多个共用同一 adapter)。
     */
    String[] value();

    MatchModeEnum mode() default MatchModeEnum.EXACT;

    DispatchGroupEnum group() default DispatchGroupEnum.DEVICE_DATA;

    /**
     * 优先级,数字越小越先匹配(同 topic 多匹配时用)。
     */
    int order() default 100;
}
