package com.mqttsnet.thinglinks.video.enumeration.jt1078;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * JT/T 1078 命令类型枚举。
 * 包含实时音视频、历史回放、语音对讲、语音广播、抓拍、控制指令等操作类型。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Jt1078CommandTypeEnum", description = "JT/T 1078命令类型枚举")
public enum Jt1078CommandTypeEnum {

    /**
     * 实时音视频（指令 0x9101）
     */
    REAL_PLAY("REAL_PLAY", "实时音视频"),

    /**
     * 历史音视频（指令 0x9201）
     */
    PLAYBACK("PLAYBACK", "历史音视频"),

    /**
     * 语音对讲（指令 0x9101 音频模式）
     */
    TALK("TALK", "语音对讲"),

    /**
     * 语音广播
     */
    BROADCAST("BROADCAST", "语音广播"),

    /**
     * 远程抓拍（指令 0x8801）
     */
    CAPTURE("CAPTURE", "抓拍"),

    /**
     * 控制指令
     */
    CONTROL("CONTROL", "控制指令"),
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
     * @param value 命令类型标识
     * @return 匹配的枚举实例，未匹配时返回 {@link Optional#empty()}
     */
    public static Optional<Jt1078CommandTypeEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
