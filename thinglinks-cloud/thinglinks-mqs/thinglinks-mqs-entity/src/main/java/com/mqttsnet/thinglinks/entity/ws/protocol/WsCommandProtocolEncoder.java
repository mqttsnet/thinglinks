package com.mqttsnet.thinglinks.entity.ws.protocol;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WebSocket 下行命令协议编码器。
 *
 * <p><b>职责</b>:把通用业务侧的"设备命令请求 vo"编码为 WS 应用层 JSON 子协议报文,
 * 让 ws 设备能像 MQTT 设备一样按统一规约解析下行命令。
 *
 * <h3>子协议规约(平台 → 设备)</h3>
 * <pre>{@code
 * { "type": "DOWN",       "topic": "...", "payload": "...", "messageId": "...", "ts": ... }
 * { "type": "KICK",       "messageId": "...", "ts": ... }
 * { "type": "RECONNECT",  "messageId": "...", "ts": ... }
 * { "type": "PONG",       "ts": ... }
 * }</pre>
 *
 * <h3>消息流</h3>
 * <pre>
 *   broker.WebSocketBrokerService.publishMessage(PublishWebSocketMessageRequestVO)
 *     → <b>本编码器</b> 把 vo 编码为子协议 JSON(producer 侧一次性编码)
 *     → RocketMQ broadcast(WsCommandBroadcastEvent,携带 encodedMessage)
 *     → broker 全节点 WsCommandDownlinkListener 消费 → 查本地 session
 *     → 持有该设备 TCP 的节点 session.sendText(json) 推给设备
 * </pre>
 *
 * <p><b>设计选择</b>:Encoder 放在 mqs-entity 共享包 ── broker / mqs 双端都可 import;
 * 当前由 broker producer 侧调用(广播前一次性编码,各节点直推同一份字节,messageId 一致)。
 *
 * @author mqttsnet
 */
public final class WsCommandProtocolEncoder {

    /**
     * 命令类型:下发数据 / 业务命令
     */
    public static final String TYPE_DOWN = "DOWN";
    /**
     * 命令类型:踢下线
     */
    public static final String TYPE_KICK = "KICK";
    /**
     * 命令类型:要求重连
     */
    public static final String TYPE_RECONNECT = "RECONNECT";
    /**
     * 心跳响应
     */
    public static final String TYPE_PONG = "PONG";
    /**
     * 复用单例 ObjectMapper(线程安全)
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private WsCommandProtocolEncoder() {
    }

    /**
     * 编码"下发命令"报文(最常用,默认类型 DOWN)。
     * <p>messageId 自动用 snowflake 生成;ts 用当前毫秒时间戳。
     *
     * @param topic   命令 topic(MQTT 风格,如 {@code $thing/down/command/{productId}/{deviceId}})
     * @param payload 业务负载(可以是 JSON 字符串 / 普通字符串 / base64 二进制)
     * @return 子协议 JSON 字符串,如 {@code {"type":"DOWN","topic":"...","payload":"...","messageId":"...","ts":...}}
     */
    public static String encodeDown(String topic, Object payload) {
        return encodeDown(topic, payload, IdUtil.fastSimpleUUID());
    }

    /**
     * 编码"下发命令"报文,带显式 messageId(便于业务侧关联 ack)。
     *
     * @param topic     命令 topic
     * @param payload   业务负载
     * @param messageId 业务唯一标识,设备 ack 中会带回此值
     * @return 子协议 JSON
     */
    public static String encodeDown(String topic, Object payload, String messageId) {
        WsProtocolMessage msg = WsProtocolMessage.builder()
            .type(TYPE_DOWN)
            .topic(topic)
            .payload(payload)
            .messageId(messageId)
            .ts(System.currentTimeMillis())
            .build();
        return toJson(msg);
    }

    /**
     * 编码"踢下线"报文。设备应在收到后主动断开 ws 连接。
     */
    public static String encodeKick() {
        return toJson(WsProtocolMessage.builder()
            .type(TYPE_KICK)
            .messageId(IdUtil.fastSimpleUUID())
            .ts(System.currentTimeMillis())
            .build());
    }

    /**
     * 编码"要求重连"报文。设备应在收到后断开重连(如服务端集群迁移 / session 失效)。
     */
    public static String encodeReconnect() {
        return toJson(WsProtocolMessage.builder()
            .type(TYPE_RECONNECT)
            .messageId(IdUtil.fastSimpleUUID())
            .ts(System.currentTimeMillis())
            .build());
    }

    /**
     * 编码"心跳响应"报文(对设备 PING 的应答)。
     */
    public static String encodePong() {
        return toJson(WsProtocolMessage.builder()
            .type(TYPE_PONG)
            .ts(System.currentTimeMillis())
            .build());
    }

    /**
     * 通用编码入口:已有 {@link WsProtocolMessage} 实例时直接 JSON 化。
     */
    public static String encode(WsProtocolMessage msg) {
        return toJson(msg);
    }

    private static String toJson(WsProtocolMessage msg) {
        try {
            return MAPPER.writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            // 极少见(POJO 字段都是基本类型 / 字符串 / Object),退化为简单 toString,不抛
            return "{\"type\":\"" + msg.getType() + "\",\"_encodeError\":\"" + e.getMessage() + "\"}";
        }
    }
}
