package com.mqttsnet.thinglinks.dto.bridge.protocol.dm;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * 达梦 DM 凭证 DTO。
 *
 * @author mqttsnet
 */
public class DmCredentialDto implements ProtocolCredentialDto {
    /**
     * 数据库密码（落盘 AES 加密）
     */
    public String password;
}
