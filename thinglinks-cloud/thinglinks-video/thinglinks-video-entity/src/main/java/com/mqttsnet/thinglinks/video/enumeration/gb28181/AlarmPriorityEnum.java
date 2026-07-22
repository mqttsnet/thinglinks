package com.mqttsnet.thinglinks.video.enumeration.gb28181;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * GB/T 28181-2016 告警级别（AlarmPriority）枚举。
 * <p>
 * 定义见标准 Section 9.4 报警通知消息体。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-19
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "AlarmPriorityEnum", description = "GB28181 告警级别枚举")
public enum AlarmPriorityEnum {

    /** 一级警情（紧急） */
    LEVEL_1(1, "一级警情"),

    /** 二级警情（重要） */
    LEVEL_2(2, "二级警情"),

    /** 三级警情（普通） */
    LEVEL_3(3, "三级警情"),

    /** 四级警情（提示） */
    LEVEL_4(4, "四级警情"),
    ;

    /** GB28181 协议值 */
    private Integer value;

    /** 中文描述 */
    private String description;

    /**
     * 根据 value 查找枚举。
     *
     * @param value 告警级别值
     * @return 匹配的枚举实例，未匹配或 value 为 null 时返回 {@link Optional#empty()}
     */
    public static Optional<AlarmPriorityEnum> fromValue(Integer value) {
        if (value == null) {
            return Optional.empty();
        }
        return Stream.of(values())
                .filter(e -> e.getValue().equals(value))
                .findFirst();
    }

    /**
     * 日志友好的描述字符串，格式 "<value>(<description>)"。
     * 非标准值或 null 时降级为原值字符串（不抛异常），便于日志打印和排查。
     *
     * @param value 告警级别值
     * @return 形如 "1(一级警情)" 的字符串；value 不匹配标准时返回 "<value>"；value 为 null 时返回 "null"
     */
    public static String descOf(Integer value) {
        if (value == null) {
            return "null";
        }
        return fromValue(value)
                .map(e -> value + "(" + e.getDescription() + ")")
                .orElse(String.valueOf(value));
    }
}
