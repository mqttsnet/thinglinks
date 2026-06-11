package com.mqttsnet.thinglinks.vo.query;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 设备下行命令(协议无关)── 业务侧(mqs 设备响应 / link 设备调试等)组完命令参数填这个,经
 * {@code DeviceDownlinkFacade} Feign 投到 broker,由 broker 按 {@link #protocolType} 分流到具体协议通道。
 *
 * <p>各协议各取所需:MQTT 用 topic/qos,WebSocket 用 clientId。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class DownlinkCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标设备产品的协议类型,取值同 {@code ProtocolTypeEnum.getValue()}:MQTT / WEBSOCKET / TCP。空或未知 → broker 兜底 MQTT。
     */
    private String protocolType;

    /**
     * 租户 ID(必填)。
     */
    private String tenantId;

    /**
     * 客户端 ID ── WebSocket 下行按它定位会话(WS 必填);MQTT 按 topic 投递,可空。
     */
    private String clientId;

    /**
     * 设备标识 ── 备用 / 日志定位。
     */
    private String deviceIdentification;

    /**
     * 下行 topic(必填)。
     */
    private String topic;

    /**
     * QoS ── 仅 MQTT;WebSocket 忽略。
     */
    private String qos;

    /**
     * 消息负载(必填)。
     */
    private String payload;

    /**
     * 是否强制 Base64 解码 ── 仅 MQTT;{@code null}=自动探测(等价历史 {@code setPayloadData})。
     */
    private Boolean forceBase64Decode;

    /**
     * 发布者类型,默认 {@code "web"}。
     */
    private String clientType;

    /**
     * 过期秒数 ── 仅 MQTT;默认 {@code "3600"}。
     */
    private String expirySeconds;

    /**
     * 可选请求 ID(雪花 / 调用方提供);空则由 broker 侧按需生成。
     */
    private Long reqId;
}
