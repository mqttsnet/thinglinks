package com.mqttsnet.thinglinks.constants.bus;

import java.util.Set;

import com.mqttsnet.thinglinks.common.constant.CommonIotConstants;

/**
 * BifroMQ → Kafka 上行 JSON 字段名集中表。bus 模块所有 JSON 字段提取必须用本接口
 * 或 {@link CommonIotConstants},<b>禁止硬编码字面量</b>。
 *
 * <p>与 {@link CommonIotConstants} 分工:Common 放全平台公共字段(clientId / tenantId / eventType 等);
 * 本接口放协议总线特有(payload / encoding / qos / heartbeatTime 等 broker 协议层)。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public interface BusKafkaJsonField {

    /**
     * 设备发布 topic 路径(MQTT/WS/TCP 通用),通常 {@code /{tenantId}/devices/{deviceId}/datas}。
     */
    String TOPIC = "topic";

    /**
     * QoS:MQTT 0/1/2;WS/TCP 默认 1。
     */
    String QOS = "qos";

    /**
     * 原始 payload(BifroMQ 默认 base64,业务侧按 {@link #ENCODING} 解)。
     */
    String PAYLOAD = "payload";

    /**
     * 原始 payload 的 hex 编码,二进制协议排查 / 时序库存储用。
     */
    String PAYLOAD_HEX = "payloadHex";

    /**
     * 原始 payload 字节长度,统计 / 限速用。
     */
    String ORIGINAL_SIZE = "originalSize";

    /**
     * payload 编码方式:UTF-8 / BASE64 / HEX / BINARY。
     */
    String ENCODING = "encoding";

    /**
     * 设备心跳时间戳(PING 专用)。
     */
    String HEARTBEAT_TIME = "heartbeatTime";

    /**
     * 设备来源网络地址(BifroMQ ClientInfo metadata 透传) ── 用于审计 / IP 溯源.
     */
    String ADDRESS = "address";

    /**
     * BifroMQ 协议级扩展字段集 ── parser 整段塞入 {@code DeviceProtocolEvent.extension},
     * 非业务核心。新加 broker 字段时在此追加。
     * <p><b>注</b>:{@code userId} / {@code address} 已提升到 {@code DeviceProtocolEvent} 顶层字段
     * (业务直接 {@code event.getUserId()} / {@code event.getAddress()}),不在此集合内.
     */
    Set<String> EXTENSION_KEYS = Set.of(
        "keepAliveTimeSeconds", "cleanSession", "sessionPresent", "ver",
        "broker", "serverId", "userSessionId", "channelId",
        "messageId", "deliveryStatus", "subQos",
        HEARTBEAT_TIME);
}
