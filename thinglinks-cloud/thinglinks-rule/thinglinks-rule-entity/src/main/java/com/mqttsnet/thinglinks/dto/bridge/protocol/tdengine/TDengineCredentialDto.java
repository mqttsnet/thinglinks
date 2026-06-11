package com.mqttsnet.thinglinks.dto.bridge.protocol.tdengine;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * TDengine 凭证 DTO。
 *
 * @author mqttsnet
 */
public class TDengineCredentialDto implements ProtocolCredentialDto {

    /**
     * 用户名（默认 root）
     */
    public String username;

    /**
     * 密码（默认 taosdata；落盘 AES 加密）
     */
    public String password;
}
