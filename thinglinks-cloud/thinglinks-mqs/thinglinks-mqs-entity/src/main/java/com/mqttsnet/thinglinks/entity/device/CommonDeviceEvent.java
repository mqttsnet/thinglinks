package com.mqttsnet.thinglinks.entity.device;

import java.util.Map;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * 设备协议事件 ── 协议中立的统一事件总线模型,替代各协议 *Event.
 * <p>
 * 上游 Handler (MqttHandler / WsHandler / TcpHandler 等) 解析完原始报文后,
 * 把字段填到本对象,通过 Spring ApplicationEvent 投到 {@code DeviceEventDispatcher},
 * Dispatcher 按 actionType 路由到对应 Processor,实现"一套主流程跨协议复用".
 *
 * @author mqttsnet
 * @since 2026-05-14
 */
@Getter
@ToString(exclude = {"rawMessage", "extension", "deviceCache"})
public class CommonDeviceEvent extends ApplicationEvent {

    /**
     * 上游协议来源(MQTT / WEBSOCKET / TCP / HTTP),仅为审计 / 桥接 envelope 透传 ── Processor 不应按此分支
     */
    private final String protocolType;
    /**
     * 动作类型(收敛于 {@link DeviceActionTypeEnum})── Dispatcher 路由依据
     */
    private final DeviceActionTypeEnum actionType;

    /**
     * MQTT clientId / WS clientId / TCP clientId,与缓存 key 对齐
     */
    private final String clientId;
    /**
     * 租户 ID(String 形态) ── 桥接 envelope / 日志透传 0 转换;
     * {@link com.mqttsnet.basic.context.ContextUtil#setTenantId(Object)} 内部自适应 String/Long,
     * 无需调用方手动 valueOf.
     */
    private final String tenantId;
    /**
     * 应用 ID(从 DeviceCacheVO 解析填回,Processor 用)
     */
    private final String appId;
    /**
     * 设备标识(从 DeviceCacheVO 解析,Processor 用)
     */
    private final String deviceIdentification;
    /**
     * 产品标识(从 DeviceCacheVO 解析,Processor 用)
     */
    private final String productIdentification;

    /**
     * PUBLISH / SUBSCRIBE / UNSUBSCRIBE 时有,其它可空
     */
    private final String topic;
    /**
     * PUBLISH 时有
     */
    private final Integer qos;
    /**
     * Base64 payload(PUBLISH 时有)
     */
    private final String payload;
    /**
     * Hex payload(PUBLISH 时有)
     */
    private final String payloadHex;
    /**
     * 编码方式 ── BASE64 / HEX / UTF-8 等
     */
    private final String encoding;
    /**
     * 原始字节大小
     */
    private final Integer originalSize;
    /**
     * 物理时间戳(epoch ms) ── 用于业务展示 / 时序入库 / 桥接 envelope 等物理时间场景.
     * <p>由 assembler 按 {@code eventUtc → eventTime} 优先级探测.缺失保持 null,下游自行兜底.
     * <p>命名为 ts 避开父类 {@link org.springframework.context.ApplicationEvent#getTimestamp()} final 方法冲突.
     */
    private final Long ts;

    /**
     * 因果时钟(HLC,64-bit) ── 仅用于状态机 event-time LWW CAS 单调写(如 connect_status).
     * <p><b>禁止作为 ms 时间戳使用</b>:HLC 数值远超 ms,误用会破坏 datetime / 时序索引.
     * 上游未透传时保持 null,syncer 自行 fallback 到 {@link #ts}.
     */
    private final Long eventHlc;

    /**
     * 事件真实发生瞬间的物理 UTC ms ── 上游在事件产生当下抓取的时钟值,纯净不 fallback.
     * <p>用于需要精确事件时间的场景(展示 / 延迟分析 / 时序对齐).一般用 {@link #ts} 即可.
     */
    private final Long eventUtc;

    /**
     * 原始 JSON 报文 ── envelope.rawMessage 全链路 0 转换透传给下游 sink
     */
    private final String rawMessage;
    /**
     * 协议私有扩展兜底 ── 未来加字段(WS 子协议 / TCP 帧序号等)不破坏 schema
     */
    private final Map<String, Object> extension;

    /**
     * 设备缓存 VO ── bus PRE 阶段({@code DeviceCacheEnricher})已解析,经 assembler 透传至此,供下游免重取。
     * 含密钥等敏感字段 → 已排除 toString / JSON 序列化。
     */
    @JsonIgnore
    @JSONField(serialize = false)
    private final DeviceCacheVO deviceCache;

    /**
     * 全字段构造(由 {@code @Builder} 生成 builder 调用进入).
     * <p>{@code source} 兜底为 {@code CommonDeviceEvent.class}(满足 {@link ApplicationEvent} 非 null 约束);
     * {@link ProtocolTypeEnum} 入参转 String 落字段,便于下游直接透传.
     */
    @Builder
    public CommonDeviceEvent(Object source,
                             ProtocolTypeEnum protocolType,
                             DeviceActionTypeEnum actionType,
                             String clientId,
                             String tenantId,
                             String appId,
                             String deviceIdentification,
                             String productIdentification,
                             String topic,
                             Integer qos,
                             String payload,
                             String payloadHex,
                             String encoding,
                             Integer originalSize,
                             Long ts,
                             Long eventHlc,
                             Long eventUtc,
                             String rawMessage,
                             Map<String, Object> extension,
                             DeviceCacheVO deviceCache) {
        super(source == null ? CommonDeviceEvent.class : source);
        this.protocolType = protocolType == null ? null : protocolType.getValue();
        this.actionType = actionType;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.appId = appId;
        this.deviceIdentification = deviceIdentification;
        this.productIdentification = productIdentification;
        this.topic = topic;
        this.qos = qos;
        this.payload = payload;
        this.payloadHex = payloadHex;
        this.encoding = encoding;
        this.originalSize = originalSize;
        // 3 个时间字段均不兜底:让下游显式判 null,避免本机时钟污染上游真相
        this.ts = ts;
        this.eventHlc = eventHlc;
        this.eventUtc = eventUtc;
        this.rawMessage = rawMessage;
        this.extension = extension;
        this.deviceCache = deviceCache;
    }
}
