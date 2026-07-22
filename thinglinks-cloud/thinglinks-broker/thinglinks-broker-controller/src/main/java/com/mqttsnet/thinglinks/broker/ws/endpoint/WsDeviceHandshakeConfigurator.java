package com.mqttsnet.thinglinks.broker.ws.endpoint;

import java.util.List;
import java.util.Map;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * 设备 WS 握手 Configurator ── 握手阶段从 query 参数提取 username / password 放入 UserProperties,
 * 供 {@code @OnOpen} 通过 {@link jakarta.websocket.EndpointConfig} 读取做账号模式认证。
 *
 * <p><b>独立顶层类</b>(对齐 link 的 {@code WebSocketAuthHeaderCaptor}):Undertow 注册 {@code @ServerEndpoint}
 * 时按类名实例化 configurator,顶层类最稳妥;内部类做 configurator 在部分容器下会注册异常。
 *
 * <p>topic 不在连接层 —— 它是逐条报文级字段(报文体 {@code {"topic","payload"}} 里带),onMsg 解析。
 *
 * @author mqttsnet
 */
public class WsDeviceHandshakeConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        Map<String, List<String>> params = request.getParameterMap();
        sec.getUserProperties().put("username", firstParam(params, "username"));
        sec.getUserProperties().put("password", firstParam(params, "password"));
    }

    /** 取 query 参数首个值;缺省空串(避免 NPE,下游按未带处理)。 */
    private static String firstParam(Map<String, List<String>> params, String key) {
        return params.getOrDefault(key, List.of("")).get(0);
    }
}
