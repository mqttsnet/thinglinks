package com.mqttsnet.thinglinks.enumeration;


import java.util.Optional;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * BifroMQ {@code EventType} 业务系统映射表 ── 决定 plugin 端把 broker 事件按何种 {@code businessSystemEventType}
 * 写到 Kafka body 的 {@code eventType} 字段供下游 mqs {@code DeviceActionTypeEnum.fromValue} 直接匹配.
 *
 * <h3>设计</h3>
 * {@code businessSystemEventType} = "" 表示 plugin 不显式声明,下游 mqs parser 会 fallback 到 topic 查表;
 * 非空时直接命中 mqs {@code DeviceActionTypeEnum},写入 {@code device_action.actionType} + 触发 {@code device.connect_status} 状态机.
 *
 * <h3>plugin 当前实际处理(共 29 个 EventType,见 {@code EventProcessorFactory} / {@code BifromqEventCollectorPluginEventProvider.TOPIC_MAP})</h3>
 * <ul>
 *   <li><b>CLIENT_CONNECTED</b> → CONNECT(mqtt.client.connected.topic)</li>
 *   <li><b>BY_CLIENT</b> → DISCONNECT(mqtt.client.disconnect.topic)</li>
 *   <li><b>KICKED</b> → KICKED(mqtt.device.kicked.topic)</li>
 *   <li><b>BY_SERVER + 17 个被动断</b>(BAD_PACKET/IDLE/SERVER_BUSY/RESOURCE_THROTTLED/CLIENT_CHANNEL_ERROR/INBOX_TRANSIENT_ERROR/
 *       INVALID_TOPIC/MALFORMED_TOPIC/INVALID_TOPIC_FILTER/MALFORMED_TOPIC_FILTER/RE_AUTH_FAILED/NO_PUB_PERMISSION/
 *       PROTOCOL_VIOLATION/EXCEED_RECEIVING_LIMIT/EXCEED_PUB_RATE/TOO_LARGE_SUBSCRIPTION/TOO_LARGE_UNSUBSCRIPTION)
 *       → CLOSE(mqtt.server.disconnect.topic)</li>
 *   <li><b>PING_REQ</b> → PING(mqtt.ping.req.topic)</li>
 *   <li><b>SUB_ACKED</b> → SUBSCRIBE(mqtt.subscription.acked.topic)</li>
 *   <li><b>UNSUB_ACKED</b> → UNSUBSCRIBE(mqtt.unsubscription.acked.topic)</li>
 *   <li><b>DISTED</b> → PUBLISH(mqtt.distribution.completed.topic):BifroMQ Standalone 无 mqtt-to-kafka connector 时,
 *       DISTED body 带完整 PUBLISH 报文(topic/qos/payload/publisher),是设备上行唯一可用来源;
 *       mqs {@code MqttDeviceDataEdgeAdapter} 把本 topic 路由到 DEVICE_DATA group 走主流程(物模型 + TDS 入库).
 *       <b>下行命令</b>(backend publisher)同样触发 DISTED,在 mqs 端 DeviceCacheEnricher cache miss 后自动跳过,无需额外过滤</li>
 *   <li><b>DIST_ERROR</b> → DISPATCH_ERROR(mqtt.distribution.error.topic):broker dispatch 失败的协议层真相命名,主要场景下行投设备失败(与 PUBLISH 不强求对称是 by design),
 *       mqs {@code MqttDistributionEdgeAdapter} 路由到 DISTRIBUTION_ACK group → DistributionResultStage 记失败 stats</li>
 *   <li><b>NOT_AUTHORIZED_CLIENT / MQTT_SESSION_START / MQTT_SESSION_STOP</b>(""):mqs 端 audit consumer log 消费</li>
 * </ul>
 *
 * <h3>3.3.5 → 4.0 升级影响清单</h3>
 * <table border="1">
 *   <tr><th>类别</th><th>EventType</th><th>4.0 变化</th></tr>
 *   <tr><td>新增 EventType</td><td>SERVER_REDIRECTED(对应 {@code Redirect} class)</td><td>3.3.5 无,4.0 升级时补 EventType 枚举值 + processor + TOPIC_MAP</td></tr>
 *   <tr><td>新增 EventType</td><td>RETAIN_MSG_MATCHED / QOS1_PUSH_ERROR / QOS2_PUSH_ERROR / SUB_STALLED / PERSISTENT_FANOUT_THROTTLED / PERSISTENT_FANOUT_BYTES_THROTTLED / GROUP_FANOUT_THROTTLED</td><td>按业务需要选择性接入</td></tr>
 *   <tr><td>移除 EventType</td><td>DELIVER_NO_INBOX / SUBSCRIBED / SUBSCRIBE_ERROR / UNSUBSCRIBED / UNSUBSCRIBED_ERROR</td><td>4.0 已删,本枚举可同步移除</td></tr>
 *   <tr><td>类字段新增</td><td>Kicked + ClientInfo kicker</td><td>{@code KickedEventProcessor} 类已加 TODO,4.0 升级时补 enrichSubtype</td></tr>
 *   <tr><td>类字段新增</td><td>InboxTransientError + String reason</td><td>{@code InboxTransientErrorEventProcessor} 类已加 TODO,4.0 升级时补 enrichSubtype</td></tr>
 *   <tr><td>包路径</td><td>全部</td><td>{@code com.baidu.bifromq} → {@code org.apache.bifromq} 全局替换</td></tr>
 * </table>
 *
 * <h3>BifroMQ 官方</h3>
 * 引用自 {@code com.baidu.bifromq.plugin.eventcollector.EventType}(3.3.5)/
 * {@code org.apache.bifromq.plugin.eventcollector.EventType}(4.0).
 *
 * @author mqttsnet
 * @since 2022-12-16
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum EventTypeEnum {
    // ════════════════════════════════════════════════════════════════════════════
    // Channel Closed Events ── 会话建立阶段失败(client 还未认证完成,无 ClientInfo).
    // BifroMQ 包:com.baidu.bifromq.plugin.eventcollector.mqttbroker.channelclosed
    // plugin 当前只注册 NOT_AUTHORIZED_CLIENT 一个(audit 用),其余 11 个 P2 跟进.
    // 这些事件不影响 device.connect_status(设备从未在线),仅做认证 / 协议错误审计.
    // ════════════════════════════════════════════════════════════════════════════
    AUTH_ERROR("AUTH_ERROR", "Authentication Error", "Client authentication failed", ""),
    ENHANCED_AUTH_ABORT_BY_CLIENT("ENHANCED_AUTH_ABORT_BY_CLIENT", "Enhanced Auth Aborted", "Enhanced authentication aborted by client", ""),
    UNAUTHENTICATED_CLIENT("UNAUTHENTICATED_CLIENT", "Unauthenticated Client", "Client failed authentication", ""),
    NOT_AUTHORIZED_CLIENT("NOT_AUTHORIZED_CLIENT", "Not Authorized Client", "Client not authorized to connect", ""),
    CHANNEL_ERROR("CHANNEL_ERROR", "Channel Error", "Channel-level error occurred", ""),
    CONNECT_TIMEOUT("CONNECT_TIMEOUT", "Connect Timeout", "Client connection timed out", ""),
    IDENTIFIER_REJECTED("IDENTIFIER_REJECTED", "Identifier Rejected", "Client identifier rejected (exceeds max length)", ""),
    MALFORMED_CLIENT_IDENTIFIER("MALFORMED_CLIENT_IDENTIFIER", "Malformed Client ID", "Client identifier has malformed UTF-8", ""),
    PROTOCOL_ERROR("PROTOCOL_ERROR", "Protocol Error", "MQTT protocol violation", ""),
    MALFORMED_USERNAME("MALFORMED_USERNAME", "Malformed Username", "Username has malformed UTF-8", ""),
    MALFORMED_WILL_TOPIC("MALFORMED_WILL_TOPIC", "Malformed Will Topic", "Will topic has malformed UTF-8", ""),
    UNACCEPTED_PROTOCOL_VER("UNACCEPTED_PROTOCOL_VER", "Unsupported Protocol", "Unsupported protocol version", ""),

    // ════════════════════════════════════════════════════════════════════════════
    // Client Connected ── 客户端成功建立 MQTT 会话(认证通过 + CONNACK 已发).
    // BifroMQ 包:com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientconnected
    // 字段(3.3.5/4.0 一致):serverId / userSessionId / keepAliveTimeSeconds / cleanSession / sessionPresent / lastWill
    // plugin → ClientConnectedEventProcessor → "mqtt.client.connected.topic" → mqs CONNECT → ONLINE
    // ════════════════════════════════════════════════════════════════════════════
    CLIENT_CONNECTED("CLIENT_CONNECTED", "Client Connected", "Client successfully connected", "CONNECT"),

    // ════════════════════════════════════════════════════════════════════════════
    // Client Disconnect Events ── 客户端已认证后断开(20 个子类型,plugin 全部注册).
    // BifroMQ 包:com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect
    //
    // Topic 分流策略:
    //   BY_CLIENT → "mqtt.client.disconnect.topic"(客户端主动断,正常退出)
    //   KICKED → "mqtt.device.kicked.topic"(同 clientId 抢占,审计可区分)
    //   其余 18 个 → "mqtt.server.disconnect.topic"(被动断,mqs 统一映射 CLOSE → OFFLINE)
    //
    // 4.0 升级关键:
    //   Kicked + ClientInfo kicker(新增,KickedEventProcessor 已加 TODO)
    //   InboxTransientError + String reason(新增,对应 processor 已加 TODO)
    //   新增 SERVER_REDIRECTED(EventType 枚举值 + Redirect class,本枚举 TODO 占位)
    // ════════════════════════════════════════════════════════════════════════════
    // 18 个被动 disconnect 子类型统一显式声明 "CLOSE" ── 跟 mqs DeviceActionTypeEnum.CLOSE 对齐:
    // mqs ProtocolKafkaPayloadParser 优先读 body eventType,显式 "CLOSE" 比空值 fallback 到 topic 查表稳健.
    // 结果:device_action 表记录 actionType=CLOSE,CLOSE.affectsConnectionStatus()=true → device.connect_status 切 OFFLINE.
    BAD_PACKET("BAD_PACKET", "Bad Packet", "Received malformed MQTT packet", "CLOSE"),
    BY_CLIENT("BY_CLIENT", "Client Disconnect", "Client initiated disconnect", "DISCONNECT"),
    BY_SERVER("BY_SERVER", "Server Disconnect", "Server initiated disconnect", "CLOSE"),
    SERVER_BUSY("SERVER_BUSY", "Server Busy", "Server too busy to handle request", "CLOSE"),
    RESOURCE_THROTTLED("RESOURCE_THROTTLED", "Resource Throttled", "Client exceeded resource limits", "CLOSE"),
    CLIENT_CHANNEL_ERROR("CLIENT_CHANNEL_ERROR", "Client Channel Error", "Client channel error occurred", "CLOSE"),
    IDLE("IDLE", "Idle Timeout", "Client disconnected due to inactivity", "CLOSE"),
    INBOX_TRANSIENT_ERROR("INBOX_TRANSIENT_ERROR", "Inbox Error", "Transient inbox error occurred", "CLOSE"),
    INVALID_TOPIC("INVALID_TOPIC", "Invalid Topic", "Invalid topic name", "CLOSE"),
    MALFORMED_TOPIC("MALFORMED_TOPIC", "Malformed Topic", "Topic has malformed UTF-8", "CLOSE"),
    INVALID_TOPIC_FILTER("INVALID_TOPIC_FILTER", "Invalid Topic Filter", "Invalid topic filter", "CLOSE"),
    MALFORMED_TOPIC_FILTER("MALFORMED_TOPIC_FILTER", "Malformed Topic Filter", "Topic filter has malformed UTF-8", "CLOSE"),
    // KICKED 保留独立 "KICKED" 语义:mqs DeviceActionTypeEnum.KICKED 也 affectsConnectionStatus → OFFLINE,
    // 但 device_action 表能区分"被踢"(同 clientId 抢占)与"通用 CLOSE",便于运维审计.
    KICKED("KICKED", "Kicked", "Client was kicked by admin", "KICKED"),
    RE_AUTH_FAILED("RE_AUTH_FAILED", "Re-auth Failed", "Client re-authentication failed", "CLOSE"),
    NO_PUB_PERMISSION("NO_PUB_PERMISSION", "No Publish Permission", "Client lacks publish permission", "CLOSE"),
    PROTOCOL_VIOLATION("PROTOCOL_VIOLATION", "Protocol Violation", "Client violated protocol rules", "CLOSE"),
    EXCEED_RECEIVING_LIMIT("EXCEED_RECEIVING_LIMIT", "Exceed Receive Limit", "Client exceeded receiving limit", "CLOSE"),
    EXCEED_PUB_RATE("EXCEED_PUB_RATE", "Exceed Publish Rate", "Client exceeded publish rate limit", "CLOSE"),
    TOO_LARGE_SUBSCRIPTION("TOO_LARGE_SUBSCRIPTION", "Too Large Subscription", "Subscription request too large", "CLOSE"),
    TOO_LARGE_UNSUBSCRIPTION("TOO_LARGE_UNSUBSCRIPTION", "Too Large Unsubscription", "Unsubscription request too large", "CLOSE"),
    OVERSIZE_PACKET_DROPPED("OVERSIZE_PACKET_DROPPED", "Oversize Packet Dropped", "Oversized packet was dropped", ""),
    // TODO 4.0 升级时新增:SERVER_REDIRECTED("SERVER_REDIRECTED", "Server Redirected", "Server redirected client to another node", "CLOSE")
    //   对应 BifroMQ 4.0 新增 class:org.apache.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.Redirect

    // ════════════════════════════════════════════════════════════════════════════
    // MQTT Broker / Message Events
    // BifroMQ 包:com.baidu.bifromq.plugin.eventcollector.mqttbroker(根目录 + pushhandling / disthandling 子包)
    // plugin 仅注册 PING_REQ + SUB_ACKED + UNSUB_ACKED(其余 QoS push / will dist / 各种 ack drop / retain 等事件 P3 跟进)
    // ════════════════════════════════════════════════════════════════════════════
    PING_REQ("PING_REQ", "Ping Request", "Client sent PINGREQ", "PING"),
    DISCARD("DISCARD", "Discard", "Message was discarded", ""),
    WILL_DISTED("WILL_DISTED", "Will Distributed", "Will message was distributed", ""),
    WILL_DIST_ERROR("WILL_DIST_ERROR", "Will Dist Error", "Error distributing will message", ""),
    QOS0_DIST_ERROR("QOS0_DIST_ERROR", "QoS0 Dist Error", "Error distributing QoS0 message", ""),
    QOS1_DIST_ERROR("QOS1_DIST_ERROR", "QoS1 Dist Error", "Error distributing QoS1 message", ""),
    QOS2_DIST_ERROR("QOS2_DIST_ERROR", "QoS2 Dist Error", "Error distributing QoS2 message", ""),

    // QoS events
    PUB_ACKED("PUB_ACKED", "Pub Acked", "Publish acknowledged (QoS1)", ""),
    PUB_ACK_DROPPED("PUB_ACK_DROPPED", "Pub Ack Dropped", "Publish ack dropped (QoS1)", ""),
    PUB_RECED("PUB_RECED", "Pub Received", "Publish received (QoS2)", ""),
    PUB_REC_DROPPED("PUB_REC_DROPPED", "Pub Rec Dropped", "Publish received dropped (QoS2)", ""),
    MSG_RETAINED("MSG_RETAINED", "Message Retained", "Message retained successfully", ""),
    RETAIN_MSG_CLEARED("RETAIN_MSG_CLEARED", "Retained Msg Cleared", "Retained message cleared", ""),
    MSG_RETAINED_ERROR("MSG_RETAINED_ERROR", "Retain Error", "Error retaining message", ""),
    MATCH_RETAIN_ERROR("MATCH_RETAIN_ERROR", "Match Retain Error", "Error matching retained messages", ""),
    QOS0_PUSHED("QOS0_PUSHED", "QoS0 Pushed", "QoS0 message pushed to client", ""),
    QOS0_DROPPED("QOS0_DROPPED", "QoS0 Dropped", "QoS0 message dropped", ""),
    QOS1_PUSHED("QOS1_PUSHED", "QoS1 Pushed", "QoS1 message pushed to client", ""),
    QOS1_DROPPED("QOS1_DROPPED", "QoS1 Dropped", "QoS1 message dropped", ""),
    QOS1_CONFIRMED("QOS1_CONFIRMED", "QoS1 Confirmed", "QoS1 message confirmed", ""),
    QOS2_PUSHED("QOS2_PUSHED", "QoS2 Pushed", "QoS2 message pushed to client", ""),
    QOS2_RECEIVED("QOS2_RECEIVED", "QoS2 Received", "QoS2 message received by client", ""),
    QOS2_DROPPED("QOS2_DROPPED", "QoS2 Dropped", "QoS2 message dropped", ""),
    QOS2_CONFIRMED("QOS2_CONFIRMED", "QoS2 Confirmed", "QoS2 message confirmed", ""),
    PUB_ACTION_DISALLOW("PUB_ACTION_DISALLOW", "Publish Disallowed", "Publish action not allowed", ""),
    SUB_ACTION_DISALLOW("SUB_ACTION_DISALLOW", "Subscribe Disallowed", "Subscribe action not allowed", ""),
    UNSUB_ACTION_DISALLOW("UNSUB_ACTION_DISALLOW", "Unsubscribe Disallowed", "Unsubscribe action not allowed", ""),
    ACCESS_CONTROL_ERROR("ACCESS_CONTROL_ERROR", "Access Control Error", "Access control check failed", ""),
    SUB_ACKED("SUB_ACKED", "Sub Acked", "Subscribe acknowledged", "SUBSCRIBE"),
    UNSUB_ACKED("UNSUB_ACKED", "Unsub Acked", "Unsubscribe acknowledged", "UNSUBSCRIBE"),

    // ════════════════════════════════════════════════════════════════════════════
    // Distribution Service Events ── 消息分发链路(broker 把上行 PUBLISH 扇出到所有订阅者).
    // BifroMQ 包:com.baidu.bifromq.plugin.eventcollector.distservice
    // plugin 仅注册 DISTED + DIST_ERROR(其它细粒度匹配 / 订阅事件高频量大,默认不发)
    //
    // 4.0 升级关键:
    //   DELIVER_NO_INBOX / SUBSCRIBED / SUBSCRIBE_ERROR / UNSUBSCRIBED / UNSUBSCRIBED_ERROR ── 4.0 已删除,本枚举届时同步移除
    //   新增 PERSISTENT_FANOUT_THROTTLED / PERSISTENT_FANOUT_BYTES_THROTTLED / GROUP_FANOUT_THROTTLED(扇出限流)
    // ════════════════════════════════════════════════════════════════════════════
    // DISTED 是 broker dist service "分发完成"回执 ── body 带完整 PUBLISH 报文(topic/qos/payload/publisher).
    // BifroMQ Standalone 无 mqtt-to-kafka connector,DISTED 是设备 PUBLISH 唯一可用来源 ──
    // businessSystemEventType 直接映射 PUBLISH,mqs MqttDeviceDataEdgeAdapter 路由到 DEVICE_DATA 主流程.
    // 注意:上下行都触发 ── 下行命令(backend publisher)走到 mqs 时 DeviceCacheEnricher cache miss 自然 skip.
    // DIST_ERROR 是 broker dispatch 失败(主要场景下行投设备失败:设备离线 / inbox 满 / QoS 路径异常).
    // businessSystemEventType="DISPATCH_ERROR" 用协议层真相命名(与 PUBLISH 不强求对称是 by design ──
    // PUBLISH 是业务命名,DISPATCH_ERROR 是技术命名,分别面向运营和运维);
    // mqs MqttDistributionEdgeAdapter → DISTRIBUTION_ACK group → DistributionResultStage 记失败 stats.
    DISTED("DISTED", "Distributed", "Message distributed successfully", "PUBLISH"),
    DIST_ERROR("DIST_ERROR", "Dist Error", "Error distributing message", "DISPATCH_ERROR"),
    DELIVER_ERROR("DELIVER_ERROR", "Deliver Error", "Error delivering message", ""),
    // TODO 4.0 升级时移除:DELIVER_NO_INBOX(4.0 已删除该 EventType)
    DELIVER_NO_INBOX("DELIVER_NO_INBOX", "No Inbox", "No inbox available for delivery", ""),
    DELIVERED("DELIVERED", "Delivered", "Message delivered successfully", ""),
    MATCHED("MATCHED", "Matched", "Subscription matched successfully", ""),
    MATCH_ERROR("MATCH_ERROR", "Match Error", "Error matching subscription", ""),
    UNMATCHED("UNMATCHED", "Unmatched", "Subscription unmatched successfully", ""),
    UNMATCH_ERROR("UNMATCH_ERROR", "Unmatch Error", "Error unmatching subscription", ""),
    // TODO 4.0 升级时移除下列 4 个:4.0 已从 EventType 枚举删除(订阅生命周期事件被 inbox service 简化)
    SUBSCRIBED("SUBSCRIBED", "Subscribed", "Client subscribed successfully", ""),
    SUBSCRIBE_ERROR("SUBSCRIBE_ERROR", "Subscribe Error", "Error during subscription", ""),
    UNSUBSCRIBED("UNSUBSCRIBED", "Unsubscribed", "Client unsubscribed successfully", ""),
    UNSUBSCRIBED_ERROR("UNSUBSCRIBED_ERROR", "Unsubscribe Error", "Error during unsubscription", ""),

    // ════════════════════════════════════════════════════════════════════════════
    // Inbox Service Events ── inbox 服务事件(消息存储 / 投递队列).
    // BifroMQ 包:com.baidu.bifromq.plugin.eventcollector.inboxservice
    // plugin 默认不发(高频细粒度事件,生产环境噪声大).
    // ════════════════════════════════════════════════════════════════════════════
    OVERFLOWED("OVERFLOWED", "Overflowed", "Inbox overflow occurred", ""),

    // ════════════════════════════════════════════════════════════════════════════
    // Retain Service Events ── retain 消息存储服务事件.
    // BifroMQ 包:com.baidu.bifromq.plugin.eventcollector.session(historical) — 实际现在分散在 retainhandling 包
    // plugin 默认不发.
    // ════════════════════════════════════════════════════════════════════════════
    OUT_OF_TENANT_RESOURCE("OUT_OF_TENANT_RESOURCE", "Out of Resources", "Tenant resource limit exceeded", ""),

    // ════════════════════════════════════════════════════════════════════════════
    // Session Lifecycle Events ── MQTT session 创建 / 销毁(transient + persistent 通用).
    // BifroMQ 包:com.baidu.bifromq.plugin.eventcollector.session
    // 字段(3.3.5/4.0 一致):sessionId
    // plugin → MqttSessionStart/StopEventProcessor → "mqtt.session.start/stop" → mqs audit consumer log 消费
    // ════════════════════════════════════════════════════════════════════════════
    MQTT_SESSION_START("MQTT_SESSION_START", "Session Start", "MQTT session started", ""),
    MQTT_SESSION_STOP("MQTT_SESSION_STOP", "Session Stop", "MQTT session stopped", "");

    /**
     * EventType Enum
     */
    private String value;

    private String name;

    private String description;

    /**
     * 业务系统事件类型
     * ThingLinks 业务系统事件类型
     */
    private String businessSystemEventType;

    /**
     * Get EventTypeEnum by value
     */
    public static Optional<EventTypeEnum> byValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }
        return Stream.of(values())
            .filter(e -> e.value.equalsIgnoreCase(value))
            .findFirst();
    }
}
