package com.mqttsnet.thinglinks.dto.bridge.protocol.rabbitmq;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * RabbitMQ 凭证 DTO。
 *
 * @author mqttsnet
 */
public class RabbitmqCredentialDto implements ProtocolCredentialDto {

    /**
     * AMQP 用户名。默认 guest
     */
    public String username;

    /**
     * AMQP 密码（落盘 AES 加密）
     */
    public String password;
}
