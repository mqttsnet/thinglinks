package com.mqttsnet.thinglinks.video.enumeration.gb28181;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * GB/T 28181 协议版本枚举，用于标识设备支持的国标协议版本。
 * 支持 GB/T 28181-2016 和 GB/T 28181-2022 两个版本，
 * 设备注册时根据 SIP 消息自动检测版本。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "GbProtocolVersionEnum", description = "GB/T 28181协议版本枚举")
public enum GbProtocolVersionEnum {

    /**
     * GB/T 28181-2016 版本
     */
    GB2016("2016", "GB/T 28181-2016"),

    /**
     * GB/T 28181-2022 版本
     */
    GB2022("2022", "GB/T 28181-2022"),
    ;

    private String value;
    private String desc;

    /**
     * 默认版本（大多数设备仍使用 2016 版本）
     */
    public static final GbProtocolVersionEnum DEFAULT = GB2016;

    /**
     * 根据 value 查找枚举
     *
     * @param value 版本标识
     * @return 匹配的枚举实例
     */
    public static Optional<GbProtocolVersionEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(v -> v.getValue().equals(value))
                .findFirst();
    }

    /**
     * 根据 value 查找枚举，找不到则返回默认版本
     *
     * @param value 版本标识
     * @return 匹配的枚举实例或默认版本
     */
    public static GbProtocolVersionEnum fromValueOrDefault(String value) {
        return fromValue(value).orElse(DEFAULT);
    }
}
