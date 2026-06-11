package com.mqttsnet.thinglinks.video.enumeration.stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * Invite 会话状态枚举。
 * 标识 SIP INVITE 会话的生命周期状态。
 * 状态流转：INIT → INVITED → CONNECTED → CLOSING → CLOSED
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "InviteSessionStatusEnum", description = "会话状态枚举")
public enum InviteSessionStatusEnum {

    /**
     * 初始化（尚未发送 INVITE）
     */
    INIT("init", "初始化"),

    /**
     * 已发送 INVITE（等待响应）
     */
    INVITED("invited", "已邀请"),

    /**
     * 已建立连接（流已就绪）
     */
    CONNECTED("connected", "已连接"),

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
    public static Optional<InviteSessionStatusEnum> fromValue(String value) {
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
    public boolean canTransitTo(InviteSessionStatusEnum target) {
        if (target == null) {
            return false;
        }
        return switch (this) {
            case INIT -> INVITED.equals(target) || CLOSED.equals(target);
            case INVITED -> CONNECTED.equals(target) || CLOSING.equals(target) || CLOSED.equals(target);
            case CONNECTED -> CLOSING.equals(target) || CLOSED.equals(target);
            case CLOSING -> CLOSED.equals(target);
            case CLOSED -> false;
        };
    }
}
