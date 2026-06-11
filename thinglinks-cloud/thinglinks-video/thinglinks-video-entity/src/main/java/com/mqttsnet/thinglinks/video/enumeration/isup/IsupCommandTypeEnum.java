package com.mqttsnet.thinglinks.video.enumeration.isup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * ISUP 命令类型枚举。
 * 定义海康 ISUP 协议支持的业务命令类型，
 * 包括实时预览、录像回放、云台控制、告警订阅等。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "IsupCommandTypeEnum", description = "ISUP命令类型枚举")
public enum IsupCommandTypeEnum {

    /**
     * 实时预览
     */
    REAL_PLAY("REAL_PLAY", "实时预览"),

    /**
     * 录像回放
     */
    PLAYBACK("PLAYBACK", "录像回放"),

    /**
     * 云台控制
     */
    PTZ_CONTROL("PTZ_CONTROL", "云台控制"),

    /**
     * 告警订阅
     */
    ALARM_SUBSCRIBE("ALARM_SUBSCRIBE", "告警订阅"),

    /**
     * 语音对讲
     */
    VOICE_TALK("VOICE_TALK", "语音对讲"),

    /**
     * 设备信息查询
     */
    DEVICE_INFO("DEVICE_INFO", "设备信息查询"),

    /**
     * 抓图
     */
    CAPTURE("CAPTURE", "抓图"),
    ;

    /**
     * 命令类型标识
     */
    private String value;

    /**
     * 中文描述
     */
    private String description;

    /**
     * 根据 value 查找枚举（忽略大小写）。
     *
     * @param value 命令类型标识值
     * @return 匹配的枚举实例，未匹配时返回 {@link Optional#empty()}
     */
    public static Optional<IsupCommandTypeEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
