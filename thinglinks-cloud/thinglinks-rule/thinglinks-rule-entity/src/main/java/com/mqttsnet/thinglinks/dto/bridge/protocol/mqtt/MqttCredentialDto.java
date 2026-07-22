package com.mqttsnet.thinglinks.dto.bridge.protocol.mqtt;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * MQTT 凭证 DTO。
 *
 * @author mqttsnet
 */
public class MqttCredentialDto implements ProtocolCredentialDto {

    /**
     * MQTT 密码（落盘 AES 加密）
     */
    public String password;

    /**
     * TLS CA 证书内容（PEM 格式；落盘 AES 加密）
     */
    public String caCert;
}
