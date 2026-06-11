package com.mqttsnet.thinglinks.dto.bridge.protocol.pulsar;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * Apache Pulsar 凭证 DTO。
 *
 * <p>Pulsar 支持多种鉴权（JWT token / TLS / OAuth2 / Athenz / Kerberos）。
 * 本 starter 优先 JWT token（最常见）。
 *
 * @author mqttsnet
 */
public class PulsarCredentialDto implements ProtocolCredentialDto {

    /**
     * JWT auth token（落盘 AES 加密；优先级最高）
     */
    public String authToken;

    /**
     * TLS 客户端证书（PEM 内容；落盘 AES 加密）
     */
    public String tlsCert;

    /**
     * TLS 客户端私钥（PEM 内容；落盘 AES 加密）
     */
    public String tlsKey;
}
