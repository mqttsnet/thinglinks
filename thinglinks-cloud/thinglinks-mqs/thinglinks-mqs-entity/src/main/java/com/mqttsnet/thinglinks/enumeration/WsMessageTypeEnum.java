package com.mqttsnet.thinglinks.enumeration;

import java.util.Arrays;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * WebSocket消息类型枚举
 *
 * @author mqttsnet
 */
@Getter
@Schema(title = "WsMessageTypeEnum", description = "WEB_SOCKET_MESSAGE_TYPE_ENUM")
public enum WsMessageTypeEnum {

    //------------------------------------------------
    @Schema(description = "普通客户端业务消息（用户操作触发）")
    CLIENT_MESSAGE(1001, "客户端消息"),

    @Schema(description = "实时数据推送消息（后台定时触发）")
    REALTIME_PUSH(1002, "实时推送"),


    //----------------------- 控制指令类别------------------
    @Schema(description = "心跳检测指令（连接保活）")
    HEARTBEAT(2001, "心跳检测"),

    @Schema(description = "连接数同步指令（集群状态更新）")
    CONNECTION_SYNC(2002, "连接同步"),

    //-------------------状态管理类别---------------
    @Schema(description = "强制断开连接指令（紧急情况）")
    FORCE_DISCONNECT(4001, "强制断线");

    private Integer value;

    private String desc;

    WsMessageTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    /**
     * 通过编码查找枚举
     */
    public static Optional<WsMessageTypeEnum> findByValue(Integer value) {
        if (value == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
            .filter(e -> e.value.equals(value))
            .findFirst();
    }
}

