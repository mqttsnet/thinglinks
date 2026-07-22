package com.mqttsnet.thinglinks.dto.bridge.protocol.iotdb;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * Apache IoTDB 凭证 DTO。
 *
 * @author mqttsnet
 */
public class IoTDbCredentialDto implements ProtocolCredentialDto {

    /**
     * 用户名（默认 root）
     */
    public String username;

    /**
     * 密码（默认 root；落盘 AES 加密）
     */
    public String password;
}
