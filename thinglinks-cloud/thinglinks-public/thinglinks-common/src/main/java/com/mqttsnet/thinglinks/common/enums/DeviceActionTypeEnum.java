package com.mqttsnet.thinglinks.common.enums;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Device action type shared by broker, mqs, link and rule modules.
 *
 * <p>The value set is the canonical contract for {@code LINK_DEVICE_ACTION_TYPE}. Rule condition
 * dropdowns should expose only the MQS-triggerable subset plus virtual business groups such as
 * {@code OFFLINE}; do not expose {@link #INBOUND} or {@link #UNKNOWN} as device-status triggers.
 */
public enum DeviceActionTypeEnum {

    PUBLISH("PUBLISH", "Device publishes data to a topic (uplink)."),
    CONNECT("CONNECT", "Client connection established."),
    DISCONNECT("DISCONNECT", "Client-initiated graceful disconnect."),
    CLOSE("CLOSE", "Server-initiated disconnect."),
    KICKED("KICKED", "Device kicked off by server."),
    HEART_TIMEOUT("HEART_TIMEOUT", "Heartbeat timeout reported by app-layer."),
    ERROR("ERROR", "Connection or protocol error event."),
    PING("PING", "Heartbeat uplink."),
    SUBSCRIBE("SUBSCRIBE", "Subscription acked."),
    UNSUBSCRIBE("UNSUBSCRIBE", "Unsubscription acked."),
    DISPATCH_ERROR("DISPATCH_ERROR", "Broker dispatch failed."),
    INBOUND("INBOUND", "Inbound event from an external subscription source."),
    UNKNOWN("UNKNOWN", "Unknown action type.");

    private final String value;
    private final String desc;

    DeviceActionTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * Case-insensitive lookup by enum value.
     *
     * @param value action value
     * @return matched action type, or empty when blank/unknown
     */
    public static Optional<DeviceActionTypeEnum> fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalizedValue = value.trim();
        return Stream.of(values())
                .filter(e -> e.value.equalsIgnoreCase(normalizedValue))
                .findFirst();
    }

    /**
     * Whether this action can affect {@code device.connect_status}.
     */
    public boolean affectsConnectionStatus() {
        return switch (this) {
            case CONNECT, DISCONNECT, CLOSE, KICKED, HEART_TIMEOUT, ERROR -> true;
            default -> false;
        };
    }

    /**
     * Whether this action should be selectable in the device-status rule trigger dictionary.
     */
    public boolean ruleConditionSelectable() {
        return this != INBOUND && this != UNKNOWN;
    }

    /**
     * Default Chinese remark for persisted device action records.
     */
    public String defaultRemark() {
        return switch (this) {
            case PUBLISH -> "设备数据上行";
            case DISPATCH_ERROR -> "Broker 分发失败(主要为下行投设备失败)";
            case CONNECT -> "客户端连接成功";
            case DISCONNECT -> "客户端主动断开连接(发送 DISCONNECT 报文)";
            case CLOSE -> "服务端关闭连接(心跳超时 / 会话过期 / ACL 拒绝 / 协议异常等 broker 侧原因)";
            case KICKED -> "客户端被踢线(同 clientId 重复登录,新连接替换旧连接)";
            case HEART_TIMEOUT -> "应用层心跳超时(WS/TCP 自定义心跳;MQTT keepalive 超时归 CLOSE)";
            case ERROR -> "协议异常事件";
            case PING -> "设备心跳上行";
            case SUBSCRIBE -> "订阅成功";
            case UNSUBSCRIBE -> "取消订阅成功";
            case INBOUND -> "入站事件(第三方订阅源)";
            default -> "未知动作";
        };
    }
}
