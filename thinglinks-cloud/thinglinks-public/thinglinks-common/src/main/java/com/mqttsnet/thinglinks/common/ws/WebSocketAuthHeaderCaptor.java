package com.mqttsnet.thinglinks.common.ws;

import java.util.List;
import java.util.Map;

import com.mqttsnet.basic.context.ContextConstants;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 握手 Configurator ── 把网关写入的 USER_ID/TENANT_ID/X-Forwarded-For/UA header
 * 转存到 {@link ServerEndpointConfig#getUserProperties()},供 onOpen 通过 EndpointConfig 读取
 * (jakarta WebSocket onOpen 签名拿不到 HandshakeRequest)。业务校验在 {@link WebSocketAuthGuard}。
 *
 * <p>用法:@ServerEndpoint(configurator = WebSocketAuthHeaderCaptor.class),
 * onOpen 内 if (!WebSocketAuthGuard.requireSameTenant(...)) return;</p>
 *
 * @author mqttsnet
 */
@Slf4j
public class WebSocketAuthHeaderCaptor extends ServerEndpointConfig.Configurator {

    /** 每次握手覆盖一次 ── onOpen 同步紧随 modifyHandshake,串行容器下无并发污染 */
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        Map<String, Object> props = config.getUserProperties();
        props.put(WebSocketUserProps.LOGIN_USER_ID, firstHeader(request, ContextConstants.USER_ID_HEADER));
        props.put(WebSocketUserProps.LOGIN_TENANT_ID, firstHeader(request, ContextConstants.TENANT_ID_HEADER));
        props.put(WebSocketUserProps.REMOTE_ADDR, firstHeader(request, "X-Forwarded-For"));
        props.put(WebSocketUserProps.USER_AGENT, firstHeader(request, "User-Agent"));

        log.info("[ws-handshake] uri={} loginUserId={} loginTenantId={} remoteAddr={}",
            request.getRequestURI(),
            props.get(WebSocketUserProps.LOGIN_USER_ID),
            props.get(WebSocketUserProps.LOGIN_TENANT_ID),
            props.get(WebSocketUserProps.REMOTE_ADDR));
    }

    /**
     * 取 header 第一个值,缺失返回 null(让 Guard 决定如何处理 null)。
     */
    private static String firstHeader(HandshakeRequest request, String headerName) {
        if (request == null || request.getHeaders() == null) {
            return null;
        }
        List<String> values = request.getHeaders().get(headerName);
        return values == null || values.isEmpty() ? null : values.get(0);
    }
}
