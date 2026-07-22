package com.mqttsnet.thinglinks.dto.bridge.protocol.redis;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * Redis 凭证 DTO。
 *
 * @author mqttsnet
 */
public class RedisCredentialDto implements ProtocolCredentialDto {

    /**
     * Redis 密码（requirepass / masterauth；空则不认证）
     */
    public String password;
}
