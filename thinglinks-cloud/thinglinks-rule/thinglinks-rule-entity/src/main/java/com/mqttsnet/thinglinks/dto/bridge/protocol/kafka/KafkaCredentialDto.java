package com.mqttsnet.thinglinks.dto.bridge.protocol.kafka;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * Kafka 凭证 DTO（SASL 鉴权；落盘 EncryptTypeHandler 加密）。
 *
 * @author mqttsnet
 */
public class KafkaCredentialDto implements ProtocolCredentialDto {

    /**
     * SASL 机制：PLAIN / SCRAM-SHA-256 / SCRAM-SHA-512。默认 SCRAM-SHA-256
     */
    public String saslMechanism;

    /**
     * SASL 用户名（由 Kafka 集群管理员分配）
     */
    public String saslUsername;

    /**
     * SASL 密码（落盘 AES 加密）
     */
    public String saslPassword;
}
