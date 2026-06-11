package com.mqttsnet.thinglinks.dto.bridge.protocol.webhook;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * WebHook 连接参数 DTO（HTTP + HMAC 签名）。
 *
 * @author mqttsnet
 */
public class WebhookConnectionDto implements ProtocolConnectionDto {

    /**
     * WebHook URL。必填
     */
    public String url;

    /**
     * Content-Type。默认 application/json
     */
    public String contentType;
}
