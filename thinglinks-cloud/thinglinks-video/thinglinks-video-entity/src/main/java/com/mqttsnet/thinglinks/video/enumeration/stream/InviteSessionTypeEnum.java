package com.mqttsnet.thinglinks.video.enumeration.stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * Invite 会话类型枚举。
 * 标识 SIP INVITE 会话的业务用途。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "InviteSessionTypeEnum", description = "会话类型枚举")
public enum InviteSessionTypeEnum {

    /**
     * 实时预览
     */
    PLAY("play", "实时预览"),

    /**
     * 录像回放
     */
    PLAYBACK("playback", "录像回放"),

    /**
     * 录像下载
     */
    DOWNLOAD("download", "录像下载"),

    /**
     * 语音广播
     */
    BROADCAST("broadcast", "语音广播"),

    /**
     * 语音对讲
     */
    TALK("talk", "语音对讲"),
    ;

    private String value;
    private String desc;

    /**
     * 根据 value 查找枚举
     *
     * @param value 枚举值
     * @return 匹配的枚举实例
     */
    public static Optional<InviteSessionTypeEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equals(value))
                .findFirst();
    }
}
