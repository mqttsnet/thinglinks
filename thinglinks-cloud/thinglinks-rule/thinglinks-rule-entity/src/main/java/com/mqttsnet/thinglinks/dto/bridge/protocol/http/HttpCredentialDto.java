package com.mqttsnet.thinglinks.dto.bridge.protocol.http;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * HTTP 凭证 DTO（Bearer Token 或 Basic Auth 二选一）。
 *
 * @author mqttsnet
 */
public class HttpCredentialDto implements ProtocolCredentialDto {

    /**
     * Bearer token（OAuth2 / JWT；优先级高于 basic auth）
     */
    public String bearerToken;

    /**
     * Basic Auth 用户名
     */
    public String basicUsername;

    /**
     * Basic Auth 密码（落盘 AES 加密）
     */
    public String basicPassword;
}
