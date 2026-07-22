package com.mqttsnet.thinglinks.video.enumeration.stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * 播放状态枚举。
 * 标识实时点播/录像回放/下载等播放场景的生命周期状态。
 * 状态流转：IDLE → INVITING → STREAMING → CLOSING → CLOSED
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "PlayStatusEnum", description = "播放状态枚举")
public enum PlayStatusEnum {

    /**
     * 空闲（未发起播放）
     */
    IDLE("idle", "空闲"),

    /**
     * 邀请中（已发送 INVITE，等待设备响应）
     */
    INVITING("inviting", "邀请中"),

    /**
     * 流传输中（RTP 流已到达流媒体服务器）
     */
    STREAMING("streaming", "流传输中"),

    /**
     * 正在关闭（已发送 BYE）
     */
    CLOSING("closing", "正在关闭"),

    /**
     * 已关闭
     */
    CLOSED("closed", "已关闭"),
    ;

    private String value;
    private String desc;

    /**
     * 根据 value 查找枚举
     *
     * @param value 枚举值
     * @return 匹配的枚举实例
     */
    public static Optional<PlayStatusEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst();
    }

    /**
     * 判断从当前状态到目标状态的转换是否合法
     *
     * @param target 目标状态
     * @return 是否允许转换
     */
    public boolean canTransitTo(PlayStatusEnum target) {
        if (target == null) {
            return false;
        }
        return switch (this) {
            case IDLE -> INVITING.equals(target);
            case INVITING -> STREAMING.equals(target) || CLOSING.equals(target) || CLOSED.equals(target);
            case STREAMING -> CLOSING.equals(target) || CLOSED.equals(target);
            case CLOSING -> CLOSED.equals(target);
            case CLOSED -> false;
        };
    }
}
