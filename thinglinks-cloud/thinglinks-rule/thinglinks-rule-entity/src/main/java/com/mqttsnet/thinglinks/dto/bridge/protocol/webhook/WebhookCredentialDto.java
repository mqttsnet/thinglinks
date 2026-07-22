package com.mqttsnet.thinglinks.dto.bridge.protocol.webhook;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * WebHook 凭证 DTO（HMAC 签名密钥）。
 *
 * @author mqttsnet
 */
public class WebhookCredentialDto implements ProtocolCredentialDto {

    /**
     * HMAC 签名密钥（落盘 AES 加密）
     */
    public String secretKey;
}
