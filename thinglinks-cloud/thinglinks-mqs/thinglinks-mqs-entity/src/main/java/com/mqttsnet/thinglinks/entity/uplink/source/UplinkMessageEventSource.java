package com.mqttsnet.thinglinks.entity.uplink.source;

import java.io.Serial;
import java.io.Serializable;

import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 设备上行消息事件源(协议中立:MQTT / WS / TCP 经统一主流程归一至此)
 * <p>
 * 字段说明：
 * - topic: 上行主题，标识消息的发布路径和消息类型
 * - qos: 服务质量等级，0-最多一次，1-至少一次，2-恰好一次
 * - payloadBytes: 原始payload字节数组，保持二进制数据的完整性
 * - payload: Base64编码的payload，便于在文本系统中传输
 * - payloadHex: 十六进制格式的payload，便于调试和设备协议解析
 * - originalSize: 原始数据大小（字节数），用于数据完整性校验
 * - encoding: 编码方式标识，说明payload字段的编码格式
 * - timestamp: 消息时间戳，记录消息的产生或接收时间
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/11/04
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UplinkMessageEventSource implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 上行主题
     * 标识消息的发布路径，通常包含设备ID和消息类型信息
     */
    private String topic;

    /**
     * 服务质量等级
     * 0: 最多一次（可能丢失，性能最高）
     * 1: 至少一次（可能重复，确保送达）
     * 2: 恰好一次（确保唯一，性能最低）
     */
    private String qos;

    /**
     * 原始payload字节数组（Base64解码）
     * 保持二进制数据的完整性，避免字符编码导致的损坏
     * 适用于需要直接处理字节数据的场景
     */
    private byte[] payloadBytes;

    /**
     * Base64编码的payload
     * 便于在JSON等文本系统中安全传输二进制数据
     * 是标准的二进制数据文本化方案
     */
    private String payload;

    /**
     * 十六进制格式的payload
     * 便于人工调试、设备协议解析和数据可视化
     * 保持数据可读性的同时避免编码问题
     */
    private String payloadHex;

    /**
     * 原始数据大小（字节数）
     * 用于数据完整性校验和性能监控
     * 便于验证数据在传输过程中是否完整
     */
    private String originalSize;

    /**
     * 编码方式标识
     * 说明payload字段的编码格式，便于下游系统正确处理
     * 通常为 "base64"，明确标识数据编码方式
     */
    private String encoding;

    /**
     * 消息时间戳
     * 记录消息的产生或接收时间，用于时序分析和故障排查
     * 格式通常为 毫秒时间戳
     */
    private String timestamp;

    /**
     * 设备缓存 VO ── 由上行 bus(InboundScriptTransformer)解析并透传,handler 可直接用免重取。
     * 可为 null(缓存未命中);与目标设备不一致时(子设备 / 网关)由 handler 兜底重取。
     */
    private DeviceCacheVO deviceCacheVO;
}