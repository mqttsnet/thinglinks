package com.mqttsnet.thinglinks.common.constant;

/**
 * 公共常量类，定义系统中广泛使用的常量字段
 *
 * @author thinglinks
 */
public class CommonIotConstants {

    /**
     * 事件类型字段名，用于JSON消息中标识事件类型
     * 例如：MQTT事件、WebSocket事件等各类事件的类型标识
     */
    public static final String EVENT_TYPE = "eventType";

    /**
     * 租户ID字段名，用于JSON消息中标识数据所属租户
     * 在多租户系统中用于数据隔离和权限控制
     */
    public static final String TENANT_ID = "tenantId";

    /**
     * 客户端ID字段名，用于标识MQTT客户端的唯一标识符
     * 在连接、断开连接等事件中用于识别特定客户端
     */
    public static final String CLIENT_ID = "clientId";

    /**
     * BifroMQ ClientInfo metadata 内的 user 字段名 ── 业务侧权限校验 / 多 user 隔离审计字段.
     * <p>通常等于设备 deviceIdentification(BifroMQ 认证后注入).
     */
    public static final String USER_ID = "userId";

    /**
     * 版本号
     */
    public static final String VERSION = "version";

    /**
     * 设备ID
     */
    public static final String DEVICE_ID = "deviceId";

    /**
     * 事件因果时钟字段名 ── 来自 broker plugin {@code Event.hlc()},64-bit Hybrid Logical Clock.
     * <p>跨节点严格因果保序的单调时钟(高 48 位 utc + 低 16 位 logical counter),
     * 是下游状态机单调写(如 connect_status)的唯一权威排序键.
     */
    public static final String EVENT_HLC = "eventHlc";

    /**
     * 事件发生瞬间的物理 UTC ms 字段名 ── 来自 broker plugin {@code Event.utc()}.
     * <p>跨节点物理时间锚点,业务展示 / 时序入库的时间字段应采用此字段而非 {@link #EVENT_TIME}.
     */
    public static final String EVENT_UTC = "eventUtc";

    /**
     * 事件时间字段名 ── broker plugin 处理事件瞬间的 {@link System#currentTimeMillis()}.
     * <p>仅人读 / debug 用,与 {@link #EVENT_UTC} 可能有几 ms 误差;禁止用于因果排序.
     */
    public static final String EVENT_TIME = "eventTime";

    /**
     * MQTT 主题字段名,用于标识发布 / 订阅消息的目标主题.
     */
    public static final String TOPIC = "topic";

    /**
     * MQTT QoS 字段名(0 / 1 / 2),发布消息时的服务质量等级.
     */
    public static final String QOS = "qos";

    /**
     * 设备上行消息体字段名 ── Base64 编码的原始 payload 字节.
     */
    public static final String PAYLOAD = "payload";

    /**
     * 设备上行消息体的 Hex 编码字段名,与 {@link #PAYLOAD} 同源,便于排查二进制报文.
     */
    public static final String PAYLOAD_HEX = "payloadHex";

    /**
     * payload 编码方式字段名(base64 / hex / utf-8 等).
     */
    public static final String ENCODING = "encoding";

    /**
     * 原始 payload 字节大小字段名(未编码前的真实长度).
     */
    public static final String ORIGINAL_SIZE = "originalSize";

}