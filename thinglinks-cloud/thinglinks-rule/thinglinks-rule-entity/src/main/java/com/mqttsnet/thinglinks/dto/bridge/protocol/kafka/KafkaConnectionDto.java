package com.mqttsnet.thinglinks.dto.bridge.protocol.kafka;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * Kafka 连接参数 DTO（{@code connection_json} 反序列化目标）。
 *
 * <p>字段名 = 前端 KafkaProtocol.connectionFields() field 名 = JSON key。
 *
 * @author mqttsnet
 */
public class KafkaConnectionDto implements ProtocolConnectionDto {

    /**
     * Bootstrap servers 地址列表，多个用英文逗号分隔（如 host1:9092,host2:9092）。必填
     */
    public String bootstrapServers;

    /**
     * Kafka 客户端 ID。空则自动生成（thinglinks-bridge-${dsId}）。可选
     */
    public String clientId;

    /**
     * 目标 topic 名称，支持模板变量 ${tenantId} ${productId}。必填
     */
    public String topic;

    /**
     * 分区策略：hash / roundRobin / sticky / manual。默认 hash
     */
    public String partitionStrategy;

    /**
     * 是否启用 TLS（启用后需配 SASL 凭证）。默认 false
     */
    public Boolean useTls;
}
