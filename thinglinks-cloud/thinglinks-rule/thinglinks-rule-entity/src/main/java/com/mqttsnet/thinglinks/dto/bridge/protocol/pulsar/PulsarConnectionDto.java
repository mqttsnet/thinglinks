package com.mqttsnet.thinglinks.dto.bridge.protocol.pulsar;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * Apache Pulsar 连接参数 DTO（Kafka 替代品，多租户原生）。
 *
 * @author mqttsnet
 */
public class PulsarConnectionDto implements ProtocolConnectionDto {

    /**
     * Broker service URL（pulsar://host:6650 或 pulsar+ssl://host:6651）。必填
     */
    public String serviceUrl;

    /**
     * Topic（persistent://tenant/namespace/topic）。必填
     */
    public String topic;

    /**
     * Producer name。空则自动生成
     */
    public String producerName;

    /**
     * 发送模式：SYNC / ASYNC。默认 ASYNC
     */
    public String sendMode;

    /**
     * 压缩类型：NONE / LZ4 / ZLIB / ZSTD / SNAPPY。默认 LZ4
     */
    public String compressionType;

    /**
     * 是否启用 batching。默认 true
     */
    public Boolean enableBatching;

    /**
     * Schema 类型：BYTES / STRING / JSON / AVRO。默认 BYTES（原始 envelope JSON byte[]）
     */
    public String schemaType;
}
