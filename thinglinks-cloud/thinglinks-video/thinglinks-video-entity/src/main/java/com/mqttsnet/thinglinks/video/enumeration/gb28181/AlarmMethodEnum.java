package com.mqttsnet.thinglinks.video.enumeration.gb28181;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * GB/T 28181-2016 告警方式（AlarmMethod）枚举。
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
@Schema(title = "AlarmMethodEnum", description = "GB28181 告警方式枚举")
public enum AlarmMethodEnum {

    /** 电话报警 */
    PHONE(1, "电话报警"),

    /** 设备报警 */
    DEVICE(2, "设备报警"),

    /** 短信报警 */
    SMS(3, "短信报警"),

    /** GPS 报警 */
    GPS(4, "GPS报警"),

    /** 视频报警 */
    VIDEO(5, "视频报警"),

    /** 设备故障报警 */
    DEVICE_FAULT(6, "设备故障报警"),

    /** 其他报警 */
    OTHER(7, "其他报警"),
    ;

    /** GB28181 协议值 */
    private Integer value;

    /** 中文描述 */
    private String description;

    /**
     * 根据 value 查找枚举。
     *
     * @param value 告警方式值
     * @return 匹配的枚举实例，未匹配或 value 为 null 时返回 {@link Optional#empty()}
     */
    public static Optional<AlarmMethodEnum> fromValue(Integer value) {
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
     * @param value 告警方式值
     * @return 形如 "5(视频报警)" 的字符串；value 不匹配标准时返回 "<value>"；value 为 null 时返回 "null"
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
