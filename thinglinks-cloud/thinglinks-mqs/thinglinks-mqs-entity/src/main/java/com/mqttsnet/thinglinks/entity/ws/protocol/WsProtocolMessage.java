package com.mqttsnet.thinglinks.entity.ws.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 应用层 JSON 子协议强类型映射。
 *
 * <p>由于 WebSocket 没有像 MQTT 那种二进制控制报文层,我们在应用层定义一份
 * <b>极简 JSON 子协议</b>,让 ws 设备能像 MQTT 设备一样跑完整链路（CONNECT / PUBLISH /
 * SUBSCRIBE / PING / 下行命令 / ack）。本类是该 JSON 的强类型载体,broker 收到 ws
 * 报文后 Jackson 反序列化为本类,再由 Adapter 转成 {@link com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent}。
 *
 * <h3>子协议规约（设备 → 平台）</h3>
 * <pre>
 * {@code
 * // ① 上线（broker 在 onOpen 自动构造,设备无需发）
 * { "type": "CONNECT" }
 *
 * // ② 心跳（设备 30s 一次,触发服务端 Redis TTL 续命）
 * { "type": "PING", "ts": 1234567890 }
 *
 * // ③ 数据上报（最常用）
 * {
 *   "type": "PUBLISH",
 *   "topic": "$thing/up/property/{productId}/{deviceId}",
 *   "payload": { "temperature": 25.5, "humidity": 60 },
 *   "qos": 1,
 *   "messageId": "uuid-...",
 *   "ts": 1234567890
 * }
 *
 * // ④ 订阅 / 取消订阅（如设备需接受下行）
 * { "type": "SUBSCRIBE",   "topic": "$thing/down/command/{productId}/{deviceId}" }
 * { "type": "UNSUBSCRIBE", "topic": "..." }
 *
 * // ⑤ 下发应答（设备收到下行后回 ack）
 * { "type": "PUBLISH_ACK", "messageId": "...", "code": 0 }
 * }
 * </pre>
 *
 * <h3>服务端响应（平台 → 设备）</h3>
 * <pre>
 * {@code
 * // ① 下发命令
 * { "type": "DOWN", "topic": "...", "payload": "...", "messageId": "...", "ts": ... }
 *
 * // ② 心跳响应
 * { "type": "PONG", "ts": ... }
 * }
 * </pre>
 *
 * <p><b>type 枚举值</b>: 与 {@code DeviceActionTypeEnum} / {@code DeviceProtocolEvent.eventType} 对齐:
 * CONNECT / CLOSE / DISCONNECT / PUBLISH / SUBSCRIBE / UNSUBSCRIBE / PING / PONG /
 * PUBLISH_ACK / HEART_TIMEOUT / DOWN / KICK / RECONNECT / ERROR
 *
 * @author mqttsnet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsProtocolMessage {

    /**
     * 报文类型（与 {@code DeviceActionTypeEnum} / {@code DeviceProtocolEvent.eventType} 对齐）。
     * <p>必填。
     */
    private String type;

    /**
     * Topic 路径(MQTT 风格)。PUBLISH / SUBSCRIBE / UNSUBSCRIBE / DOWN 时使用。
     * <p>CONNECT / PING / PONG 等可空。
     */
    private String topic;

    /**
     * 业务负载。
     * <p><b>注意</b>: 用 {@code Object} 承载,既支持 JSON 对象（如 {@code {"temperature": 25.5}}）
     * 也支持字符串（如 base64 字节）。Jackson 反序列化时按 JSON 实际类型保留。
     */
    private Object payload;

    /**
     * QoS 级别(应用层定义)。默认 1(at-least-once)。
     * <p>0 = fire-forget,1 = at-least-once;不支持 MQTT 的 2(exactly-once,WS 应用层难保证)。
     */
    private Integer qos;

    /**
     * 消息唯一标识(uuid)。
     * <p>PUBLISH 时设备生成,用于 PUBLISH_ACK 关联;DOWN 时服务端生成,设备 ack 中带回。
     */
    private String messageId;

    /**
     * PUBLISH_ACK 时的应答码。0 = 成功;非 0 = 业务失败码。
     * <p>非 ACK 报文为 null。
     */
    private Integer code;

    /**
     * 客户端时间戳(毫秒)。设备打,服务端不强校验;接入端时间用 {@code DeviceProtocolEvent.eventUtc}。
     */
    private Long ts;
}
