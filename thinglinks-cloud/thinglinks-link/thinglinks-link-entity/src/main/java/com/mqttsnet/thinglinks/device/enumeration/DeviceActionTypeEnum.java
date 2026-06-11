package com.mqttsnet.thinglinks.device.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 设备动作类型枚举(跨协议统一)── MQTT / WebSocket / TCP / 第三方桥接入站共用。
 *
 * <h3>与 BifroMQ event-collector plugin 对齐</h3>
 * <table border="1">
 *   <tr><th>enum</th><th>plugin EventType</th><th>topic / 来源</th></tr>
 *   <tr><td>PUBLISH</td><td>DISTED</td><td>BifroMQ Standalone:plugin DISTED → {@code mqtt.distribution.completed.topic}(body 带完整 PUBLISH 报文);或外部 mqtt-to-kafka connector → {@code thinglinks-mqs-mqttMsg}</td></tr>
 *   <tr><td>CONNECT</td><td>CLIENT_CONNECTED</td><td>mqtt.client.connected.topic</td></tr>
 *   <tr><td>DISCONNECT</td><td>BY_CLIENT</td><td>mqtt.client.disconnect.topic(客户端主动 DISCONNECT 报文)</td></tr>
 *   <tr><td>CLOSE</td><td>BY_SERVER + 17 被动断</td><td>mqtt.server.disconnect.topic(broker 关连接:keepalive 超时 / ACL / 协议错误等)</td></tr>
 *   <tr><td>KICKED</td><td>KICKED</td><td>mqtt.device.kicked.topic(同 clientId 重复登录抢占)</td></tr>
 *   <tr><td>PING</td><td>PING_REQ</td><td>mqtt.ping.req.topic(MQTT keepalive 心跳上行)</td></tr>
 *   <tr><td>HEART_TIMEOUT</td><td>—</td><td>mqs 应用层心跳超时(WS / TCP 自定义,MQTT keepalive 超时归 CLOSE)</td></tr>
 *   <tr><td>ERROR</td><td>—</td><td>mqs 协议异常(WS @OnError / TCP exception);plugin 17 被动断已归 CLOSE</td></tr>
 *   <tr><td>SUBSCRIBE</td><td>SUB_ACKED</td><td>mqtt.subscription.acked.topic</td></tr>
 *   <tr><td>UNSUBSCRIBE</td><td>UNSUB_ACKED</td><td>mqtt.unsubscription.acked.topic</td></tr>
 *   <tr><td>DISPATCH_ERROR</td><td>DIST_ERROR</td><td>mqtt.distribution.error.topic(broker dispatch 失败的技术真相命名,主要场景:下行命令投设备失败 ── 设备离线 / inbox 满 / QoS 路径异常等;与 PUBLISH 不强求对称是 by design ── PUBLISH 是业务命名,DISPATCH_ERROR 是协议层真相命名)</td></tr>
 *   <tr><td>INBOUND</td><td>—</td><td>第三方订阅源生命周期(SubscriptionSourceLifecycleManager)</td></tr>
 *   <tr><td>UNKNOWN</td><td>—</td><td>{@link #fromValue} 未匹配兜底</td></tr>
 * </table>
 *
 * <h3>字典对齐</h3>
 * {@code LINK_DEVICE_ACTION_TYPE} 字典与本枚举一一对齐 ── 删除枚举值时同步清理字典数据(运营后台,本仓库无 seed)。
 *
 * <h3>映射入口</h3>
 * BifroMQ topic → 本枚举见 {@code ProtocolKafkaPayloadParser#buildTopicEventMap}。
 *
 * @author shihuan sun
 * @since 2023-08-20
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "DeviceActionTypeEnum", description = "设备动作类型 枚举")
public enum DeviceActionTypeEnum {

    // ── 上行数据(业务命名;实际来源 broker DISTED 借用)──
    PUBLISH("PUBLISH", "Device publishes data to a topic (uplink). BifroMQ Standalone source: plugin DISTED → mqtt.distribution.completed.topic."),

    // ── 连接生命周期(affectsConnectionStatus = true)──
    CONNECT("CONNECT", "Client connection established (BifroMQ mqtt.client.connected.topic)."),
    DISCONNECT("DISCONNECT", "Client-initiated graceful disconnect (BifroMQ mqtt.client.disconnect.topic) ── client sends MQTT DISCONNECT packet."),
    CLOSE("CLOSE", "Server-initiated disconnect (BifroMQ mqtt.server.disconnect.topic) ── broker actively closes the connection, e.g. keepalive timeout / session expired / ACL deny / protocol error."),
    KICKED("KICKED", "Device kicked off by server (BifroMQ mqtt.device.kicked.topic) ── clientId duplicated, replaced by new connection."),
    HEART_TIMEOUT("HEART_TIMEOUT", "Heartbeat timeout reported by app-layer (WS / TCP custom). MQTT keepalive timeout is reported as CLOSE."),
    ERROR("ERROR", "Connection or protocol error event reported by mqs (MQTT @OnError / WS @OnError / TCP exception)."),

    // ── 心跳 ──
    PING("PING", "Heartbeat uplink (BifroMQ mqtt.ping.req.topic)."),

    // ── 订阅 ──
    SUBSCRIBE("SUBSCRIBE", "Subscription acked (BifroMQ mqtt.subscription.acked.topic)."),
    UNSUBSCRIBE("UNSUBSCRIBE", "Unsubscription acked (BifroMQ mqtt.unsubscription.acked.topic)."),

    // ── Broker 分发失败(协议层真相命名,与 PUBLISH 不强求对称是 by design)──
    DISPATCH_ERROR("DISPATCH_ERROR", "Broker dispatch failed (BifroMQ DIST_ERROR → mqtt.distribution.error.topic) ── primarily downlink to device subscriber failure: device offline / inbox full / QoS path error."),

    // ── 桥接入站 ──
    INBOUND("INBOUND", "Inbound event from an external subscription source (third-party Kafka / MQTT / HTTP push)."),

    // ── 兜底 ──
    UNKNOWN("UNKNOWN", "Unknown action type.");

    private String value;
    private String desc;

    /**
     * 按 value 字符串反查枚举(替代旧的 {@code MqttEventEnum / WsEventEnum / TcpEventEnum} 离散方法)。
     *
     * @param value 事件 value 字符串(枚举常量名)
     * @return 匹配枚举的 {@link Optional};空字符串或未匹配返 {@link Optional#empty()}
     */
    public static Optional<DeviceActionTypeEnum> fromValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }
        return Stream.of(values())
                .filter(e -> e.value.equalsIgnoreCase(value))
                .findFirst();
    }

    /**
     * 是否影响 {@code device.connect_status} 字段 ── 连接生命周期事件返 true。
     */
    public boolean affectsConnectionStatus() {
        return switch (this) {
            case CONNECT, DISCONNECT, CLOSE, KICKED, HEART_TIMEOUT, ERROR -> true;
            default -> false;
        };
    }

    /**
     * session 权威查询不可达时的兜底状态:CONNECT → ONLINE,其它 → OFFLINE。
     * 仅当 {@link #affectsConnectionStatus()} 为 true 时有意义。
     */
    public DeviceConnectStatusEnum fallbackConnectStatus() {
        return CONNECT.equals(this)
                ? DeviceConnectStatusEnum.ONLINE
                : DeviceConnectStatusEnum.OFFLINE;
    }

    /**
     * {@code DeviceAction.remark} 字段的默认中文描述,调用方可拼接 clientId / topic / protocol 上下文。
     *
     * @return 当前枚举的默认中文描述
     */
    public String defaultRemark() {
        return switch (this) {
            case PUBLISH                -> "设备数据上行";
            case DISPATCH_ERROR         -> "Broker 分发失败(主要为下行投设备失败)";
            case CONNECT                -> "客户端连接成功";
            case DISCONNECT             -> "客户端主动断开连接(发送 DISCONNECT 报文)";
            case CLOSE                  -> "服务端关闭连接(心跳超时 / 会话过期 / ACL 拒绝 / 协议异常等 broker 侧原因)";
            case KICKED                 -> "客户端被踢线(同 clientId 重复登录,新连接替换旧连接)";
            case HEART_TIMEOUT          -> "应用层心跳超时(WS/TCP 自定义心跳;MQTT keepalive 超时归 CLOSE)";
            case ERROR                  -> "协议异常事件";
            case PING                   -> "设备心跳上行";
            case SUBSCRIBE              -> "订阅成功";
            case UNSUBSCRIBE            -> "取消订阅成功";
            case INBOUND                -> "入站事件(第三方订阅源)";
            default                     -> "未知动作";
        };
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
