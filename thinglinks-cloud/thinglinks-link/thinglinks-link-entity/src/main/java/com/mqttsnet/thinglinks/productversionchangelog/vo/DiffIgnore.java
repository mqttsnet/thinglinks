package com.mqttsnet.thinglinks.productversionchangelog.vo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 ResultVO 字段在变更记录 diff 时被忽略 ── 主要用于结构外键(productId / serviceId / commandId)
 * 与字典回显辅助容器(echoList)等非业务字段。跨模块共享的父类字段忽略由
 * {@code EntityFieldDiffer.FRAMEWORK_EXCLUDE} 静态集合兜底,不依赖本注解。
 * 不影响 JSON 序列化:仅反射 diff 时读取,不带 @JsonIgnore 语义,字段值仍出现在 HTTP 响应里。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.productversion.diff.EntityFieldDiffer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DiffIgnore {
}
