package com.mqttsnet.thinglinks.entity.protocol;

import java.util.Map;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 设备协议无关上行事件统一包络（Canonical Form）。
 *
 * <p><b>设计目的</b>：把 MQTT / WebSocket / TCP / CoAP / LwM2M 等不同协议的设备
 * 上行事件归一化成同一种数据结构。下游业务（DeviceAction 持久化、物模型解析、
 * 时序入库、北向桥接、规则引擎、告警）只感知本类型，<b>无需 if mqtt then ... else if ws ...</b>，
 * 实现真正的协议解耦。
 *
 * <p><b>使用模式</b>（Adapter + Canonical Model）：
 * <pre>
 *   Kafka topic → {Mqtt|Ws|Tcp}KafkaInboundConsumer
 *      ↓ ProtocolEdgeAdapter（按 topic 路由）
 *   DeviceProtocolEvent（本类）
 *      ↓ bus 流水线编排 → Stages
 *      ├─ DeviceCacheEnricher（PRE：富化设备缓存）
 *      ├─ DeviceBizDispatchStage（CORE：委派 biz 领域处理 = 解码入库 / 状态 / 落库)
 *      └─ BridgeRelayStage → MqsBridgeEventProducer.publishBridgeEvent(...)（POST）
 * </pre>
 *
 * <p><b>新协议接入流程</b>：写一个 ProtocolEdgeAdapter（协议端 → DeviceProtocolEvent），下游全部自动 work。
 *
 * @author mqttsnet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceProtocolEvent {

    // ========== 协议元信息 ==========

    /**
     * 协议类型：MQTT / WEBSOCKET / TCP / COAP / LWM2M / ...
     * <p>由 Adapter 在转换时注入；下游可用此区分协议来源（如统计 / 告警维度）。
     */
    private String protocolType;

    /**
     * 事件类型（与 {@code DeviceActionTypeEnum} 对齐）：
     * CONNECT / CLOSE / DISCONNECT / PUBLISH / SUBSCRIBE / UNSUBSCRIBE /
     * PING / PUBLISH_ACK / HEART_TIMEOUT / CLUSTER / ERROR
     */
    private String eventType;

    // ========== 设备 / 租户身份 ==========

    /**
     * 租户 ID（多租户隔离 / DB 路由用）
     */
    private String tenantId;

    /**
     * 应用 ID(业务隔离维度)。
     * <p>由 Enricher 阶段从 DeviceCacheVO 填入,桥接 / 告警规则按 appId 维度过滤。
     */
    private String appId;

    /**
     * 协议层 clientId（broker 接入时分配；MQTT 是 connect packet 的 clientId）
     */
    private String clientId;

    /**
     * broker 认证通过后的 user 标识(plugin 从 ClientInfo metadata 透传).
     * <p>跟 {@link #deviceIdentification}(业务设备唯一标识)区分:
     * userId 是 broker 视角的用户 / 设备身份(BifroMQ 通常用 deviceIdentification 当 userId).
     * 业务侧权限校验 / 多 user 隔离审计可直接用本字段,避免反复 parse rawMessage.
     * <p>4.0 字段稳定:BifroMQ ClientInfo metadataMap 在 4.0 仍含 userId,无迁移成本.
     */
    private String userId;

    /**
     * 设备来源网络地址(plugin 从 ClientInfo metadata 透传),形如 {@code /192.168.1.10:51234}.
     * <p>安全审计 / IP 溯源 / 地理位置分析的基础字段;
     * 出现异常断开时,运维可按本字段快速定位攻击源 / 异常设备群.
     * <p>4.0 字段稳定:BifroMQ ClientInfo metadataMap 在 4.0 仍含 address,无迁移成本.
     */
    private String address;

    /**
     * 平台层设备唯一标识（业务侧 deviceIdentification，握手时已认证拿到）
     */
    private String deviceIdentification;

    /**
     * 设备所属产品标识
     */
    private String productIdentification;

    // ========== 报文内容（PUBLISH 等带数据事件填）==========

    /**
     * MQTT topic / WS 子协议 topic / TCP 不适用。
     * <p>CONNECT / PING 等无 topic 事件可空。
     */
    private String topic;

    /**
     * QoS 级别。
     * <ul>
     *   <li>MQTT：0 / 1 / 2</li>
     *   <li>WS：默认 1（应用层定义）</li>
     *   <li>TCP：通常空</li>
     * </ul>
     */
    private Integer qos;

    /**
     * 原始报文。文本类协议（MQTT JSON / WS JSON）直接放字符串；
     * 二进制协议建议 base64 编码后放字符串。
     */
    private String payload;

    /**
     * 原始报文 hex 表示（便于二进制协议排查 / 时序库存储）
     */
    private String payloadHex;

    /**
     * 原始报文字节长度（供统计 / 限速使用）
     */
    private Integer originalSize;

    /**
     * 编码：UTF-8 / BASE64 / HEX / BINARY。
     * <p>下游解析器据此决定是否对 payload 做 base64-decode / hex-decode。
     */
    private String encoding;

    // ========== 时间 / 链路 ==========

    /**
     * 入站本地时间戳(毫秒) ── adapter 创建事件的瞬间。
     * <p>用作 {@code currentTimeMillis - ts} 计算 mqs 入站到 dispatcher 入口的端到端时延。
     */
    private Long ts;

    /**
     * 全链路追踪 ID。从接入端生成,贯穿 mqs → RocketMQ → rule → sink,便于桥接日志关联
     */
    private String traceId;

    /**
     * 上游因果时钟 HLC(BifroMQ event-collector 插件注入,单调递增).
     * <p>biz {@code DeviceConnectStatusSyncHook} 用作 event-time LWW CAS 单调写键,
     * 防止异步消费 / 乱序 / 抖动重连导致 {@code device.connect_status} 回退.
     * <p><b>禁止</b>当 epoch ms 用 ── 数值远超 ms,误用会撑爆 datetime / 时序索引.
     */
    private Long eventHlc;

    /**
     * 设备事件真实发生瞬间(epoch ms,跨节点物理时间锚点).
     * <p>权威业务时间 ── 业务展示 / 时序入库 / 桥接下游数据源时间字段应一律采用此字段,
     * 与 {@link #ts}(入站本地瞬间)/ {@code eventTime}(plugin 处理时刻,debug 用)区分.
     */
    private Long eventUtc;

    // ========== 透传报文(byte 级保真) ==========

    /**
     * 上游 Kafka 原始报文 JSON 字符串(byte 级保真)。
     * <p>桥接 / 旁路通路直接透传此字段到下游 RocketMQ,避免 JSON↔Object 反复转换导致字段丢失。
     */
    private String rawMessage;

    // ========== 扩展位 ==========

    /**
     * 协议特异扩展属性（如 MQTT properties / WS messageId / TCP frameId 等）。
     * <p>使用 {@code Map<String, Object>} 兼容任意扩展;序列化时 NON_NULL 过滤,无副作用。
     * <p>避免在此放业务字段（业务字段应升级到主属性中,保持包络稳定）。
     */
    private Map<String, Object> extension;

    /**
     * 设备缓存 VO ── PRE 阶段 {@code DeviceCacheEnricher} 解析后挂这,经 assembler 透传到
     * {@code CommonDeviceEvent},下游(转换器 / handler)免重取。含密钥等敏感字段 → 排除 toString / JSON 序列化。
     */
    @JsonIgnore
    @JSONField(serialize = false)
    @ToString.Exclude
    private DeviceCacheVO deviceCache;
}
