package com.mqttsnet.thinglinks.dto.bridge.protocol.http;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * HTTP 连接参数 DTO。
 *
 * @author mqttsnet
 */
public class HttpConnectionDto implements ProtocolConnectionDto {

    /**
     * 完整 URL，支持 ${} 模板（如 https://api.example.com/iot）。必填
     */
    public String url;

    /**
     * HTTP 方法：POST / PUT / PATCH。默认 POST
     */
    public String method;

    /**
     * Content-Type。默认 application/json
     */
    public String contentType;
}
