package com.mqttsnet.thinglinks.video.enumeration.stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * 回放控制枚举。
 * 定义录像回放支持的控制操作类型。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "PlaybackControlEnum", description = "回放控制枚举")
public enum PlaybackControlEnum {

    PAUSE("pause", "暂停"),
    RESUME("resume", "恢复"),
    SEEK("seek", "拖拽"),
    SPEED("speed", "倍速"),
    ;

    private String value;
    private String desc;

    /**
     * 根据 value 查找枚举
     *
     * @param value 枚举值
     * @return 匹配的枚举实例
     */
    public static Optional<PlaybackControlEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
