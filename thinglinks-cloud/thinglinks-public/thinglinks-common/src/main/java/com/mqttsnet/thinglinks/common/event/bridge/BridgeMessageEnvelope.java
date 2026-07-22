package com.mqttsnet.thinglinks.common.event.bridge;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 桥接业务消息体 ── 跨服务（mqs → RocketMQ → rule）传递的统一信封。
 *
 * <h3>定位</h3>
 * <ul>
 *   <li><b>不是</b> DB 实体（不存表），不带 {@code @TableName} 注解</li>
 *   <li><b>是</b> RocketMQ 消息 body 的强类型映射（业务侧序列化为 JSON byte[] 后塞 RocketMQ）</li>
 *   <li>包含 IoT 业务字段（productId / deviceId / actionType / topic 等），<b>区别于</b>
 *       thinglinks-util {@code com.mqttsnet.basic.databridge.model.ConnectorPayload}（通用 byte[]+Map）</li>
 * </ul>
 *
 * <h3>透传原则</h3>
 * 设备消息体走 {@link #rawMessage} 字段(String),持有<b>上游 Kafka 消息的整原报文(JSON 字符串)</b>。
 * Kafka StringDeserializer 已经把 bytes UTF-8 解码成 String,mqs listener 直接
 * {@code .rawMessage(event.getMessage())} 透传 ── 全链路 0 转换;
 * {@code SinkDispatcher} 投递下游时唯一一次 {@code rawMessage.getBytes(UTF-8)} 转 byte[](Sink body 是字节),
 * 由于 Kafka StringSerializer 强制 UTF-8,该操作与上游写入 Kafka 时的原始 bytes 字节级等价。
 * 其它字段(traceId / tenantId / device 等)仅供规则匹配 + 审计 trace 用,不进 body。
 *
 * <h3>放置位置</h3>
 * 放在 thinglinks-common 是因为<b>多个服务都需要构造/解析此对象</b>:
 * <ul>
 *   <li>thinglinks-mqs-biz:构造并通过 RocketMQ 投递到 {@code thinglinks-bridge-device-event}</li>
 *   <li>thinglinks-rule-biz:消费 RocketMQ 后反序列化为本对象,做规则匹配</li>
 *   <li>thinglinks-rule-controller / SubscriptionSourceManager:入站时构造本对象后投 ingress topic</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BridgeMessageEnvelope implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ============================== 链路追踪 ==============================

    /**
     * 全链路追踪 ID（与 mqs 设备 publish 日志串联）。来自 ContextUtil.getLogTraceId()
     */
    private String traceId;

    // ============================== 多租户上下文 ==============================

    /**
     * 租户 ID
     */
    private String tenantId;

    // ============================== 设备身份(规则匹配用)==============================

    /**
     * 应用 ID(多租户应用维度;来自 DeviceCacheVO.appId / Device.app_id 字段)。
     * <p>桥接规则按 (tenantId + appId + direction) 维度索引;{@code BridgeRuleEnabledCacheKeyBuilder} key 段。
     */
    private String appId;

    /**
     * 产品标识(规则匹配 productIdentifications 用)
     */
    private String productIdentification;

    /**
     * 设备标识(规则匹配 deviceIdentifications 用 + Sink routingKey 用)
     */
    private String deviceIdentification;

    /**
     * 客户端连接 ID(mqs 协议层),trace 串联用
     */
    private String clientId;

    /**
     * broker 认证通过后的 user 标识(plugin 从 ClientInfo metadata 透传).
     * <p>跟 {@link #deviceIdentification}(业务设备唯一标识)区分:userId 是 broker 视角的用户身份.
     * 下游 rule sink / 第三方业务系统的权限校验 / 多 user 隔离审计可直接用,避免反复 parse rawMessage.
     */
    private String userId;

    /**
     * 设备来源网络地址(plugin 从 ClientInfo metadata 透传),形如 {@code /192.168.1.10:51234}.
     * <p>下游业务可用作:安全审计 / IP 溯源 / 地理位置分析 / 攻击源定位.
     */
    private String address;

    // ============================== 协议事件元数据 ==============================

    /**
     * 事件类型枚举(PUBLISH/CONNECT/CLOSE/DISCONNECT/SUBSCRIBE/UNSUBSCRIBE/PING/PUBLISH_ACK/HEART_TIMEOUT/ERROR/...)。
     * 复用字典 LINK_DEVICE_ACTION_TYPE,与 {@code com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum} 一一对齐 ──
     * 所有协议(MQTT/WS/TCP)的事件类型都收敛到此一个枚举。
     * <p>规则匹配 actionTypes 用。
     */
    private String actionType;

    /**
     * 协议类型(MQTT / WEBSOCKET / TCP / COAP / LWM2M ...)。
     * <p>未来若要按协议过滤规则可用,目前主要 trace 元数据。
     */
    private String protocolType;

    /**
     * 设备事件 topic(PUBLISH 时有;CONNECT/PING 等可空)。
     * <p>规则匹配 topicPatterns 用。
     */
    private String topic;

    // ============================== 时间戳 ==============================

    /**
     * 事件发生时间(epoch millis)
     */
    private Long ts;

    /**
     * 上游因果时钟 HLC(BifroMQ event-collector 插件注入,单调递增).
     * <p>来自 {@code DeviceProtocolEvent.eventHlc};由 mqs {@code BridgeRelayStage} 透传到本字段.
     * <p>下游 rule 端可据此 dedup / 排序 / CAS 单调写,与 {@code device.last_status_event_hlc} 同一 stream;
     * 非设备上行事件(rule 入站 source / 内部构造)可空.
     * <p><b>禁止</b>当 epoch ms 用 ── 数值远超 ms,误用会撑爆 datetime / 时序索引.
     */
    private Long eventHlc;

    /**
     * 设备事件真实发生瞬间(epoch ms,跨节点物理时间锚点).
     * <p>来自 {@code DeviceProtocolEvent.eventUtc};由 mqs {@code BridgeRelayStage} 透传到本字段.
     * <p>桥接下游数据源时间字段(InfluxDB measurement time / Kafka timestamp / 第三方业务表的 occur_time 等)
     * 应一律采用此字段,与 {@link #ts}(envelope 构造瞬间)区分.
     */
    private Long eventUtc;

    // ============================== 原始报文(透传给下游)==============================

    /**
     * 上游 Kafka 整原报文(JSON 字符串),全链路 0 转换字节级保真透传给下游 Sink。
     * <p>
     * <b>设计</b>:
     * <ul>
     *   <li>mqs listener 直接 {@code .rawMessage(event.getMessage())} ── Kafka StringDeserializer
     *       已经把 bytes UTF-8 解码成 String,这里不再做任何转换</li>
     *   <li>RocketMQ JSON 传输:envelope 整体 JSON 序列化时,本字段以 string 转义嵌入
     *       (Jackson escape/unescape 标准机制,无损 round-trip)</li>
     *   <li>rule {@code SinkDispatcher.toConnectorPayload}:body = {@code rawMessage.getBytes(UTF-8)},
     *       由于 Kafka StringSerializer 强制 UTF-8,该操作与上游写入 Kafka 时的原始 bytes 字节级等价</li>
     *   <li>第三方 Sink 收到 = 上游 BifroMQ Kafka 原字节</li>
     * </ul>
     * <b>不要</b>在 envelope 上再单独抽 base64/hex/encoding 字段 ── 上游报文里有什么由消费者自行解析。
     */
    private String rawMessage;

    // ============================== 数据形态(物模型出站)==============================

    /**
     * 报文数据形态:原始报文。
     */
    public static final String PAYLOAD_KIND_RAW = "RAW";
    /**
     * 报文数据形态:物模型匹配后的结构化数据。
     */
    public static final String PAYLOAD_KIND_THING_MODEL = "THING_MODEL";

    /**
     * 报文数据形态 ── {@code RAW}(原始报文,默认)/ {@code THING_MODEL}(物模型匹配后的结构化数据)。
     * <p>设备上行 PUBLISH 经物模型解析落库后,额外旁路一条 {@code THING_MODEL} 事件,其结构化数据
     * (产品 + 服务 + 属性值)直接放在 {@link #rawMessage}(与 RAW 统一,Sink / transform / trace 0 改动);
     * 其余事件为 {@code RAW}。
     * <p>桥接规则按 matchConfig 的 {@code payloadKinds} 选择消费哪种形态,缺省只匹配 {@code RAW},
     * 避免存量规则把物模型事件重复处理。
     */
    private String payloadKind;
}
