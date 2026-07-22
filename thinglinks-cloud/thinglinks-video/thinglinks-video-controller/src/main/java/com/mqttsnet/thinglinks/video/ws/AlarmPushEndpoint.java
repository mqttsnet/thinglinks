package com.mqttsnet.thinglinks.video.ws;

import com.mqttsnet.thinglinks.common.ws.WebSocketAuthGuard;
import com.mqttsnet.thinglinks.common.ws.WebSocketAuthHeaderCaptor;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 告警实时推送 WebSocket 端点(会话管理委托 {@link VideoWebSocketSessionHolder});
 * onOpen 校验 path tenantId == 登录 tenantId,防跨租户拿他人告警流。
 *
 * @author mqttsnet
 */
@ServerEndpoint(
        value = "/anyone/videoSocket/alarm/{tenantId}",
        configurator = WebSocketAuthHeaderCaptor.class
)
@Component
@Slf4j
public class AlarmPushEndpoint {

    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("tenantId") String tenantId) {
        if (!WebSocketAuthGuard.requireSameTenant(session, config, tenantId)) {
            return;
        }
        VideoWebSocketSessionHolder.addAlarmSession(tenantId, session);
        log.info("[WS-告警推送] 连接建立, sessionId={}, tenantId={}", session.getId(), tenantId);
    }

    @OnMessage
    public String onMessage(String text, @PathParam("tenantId") String tenantId) {
        return "ping".equals(text) ? "pong" : "";
    }

    @OnClose
    public void onClose(Session session, @PathParam("tenantId") String tenantId) {
        VideoWebSocketSessionHolder.removeAlarmSession(tenantId, session);
        log.info("[WS-告警推送] 连接关闭, sessionId={}, tenantId={}", session.getId(), tenantId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.warn("[WS-告警推送] 连接异常, sessionId={}, error={}", session.getId(), error.getMessage());
    }
}
