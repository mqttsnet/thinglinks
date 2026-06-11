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
 * 设备状态变更 WebSocket 端点(会话管理委托 {@link VideoWebSocketSessionHolder},
 * 避免 video-biz Pusher 反向依赖 video-controller);onOpen 校验 path tenantId == 登录 tenantId。
 *
 * @author mqttsnet
 */
@ServerEndpoint(
        value = "/anyone/videoSocket/deviceStatus/{tenantId}",
        configurator = WebSocketAuthHeaderCaptor.class
)
@Component
@Slf4j
public class DeviceStatusEndpoint {

    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("tenantId") String tenantId) {
        if (!WebSocketAuthGuard.requireSameTenant(session, config, tenantId)) {
            return;
        }
        VideoWebSocketSessionHolder.addDeviceStatusSession(tenantId, session);
        log.info("[WS-设备状态] 连接建立, sessionId={}, tenantId={}", session.getId(), tenantId);
    }

    @OnMessage
    public String onMessage(String text, @PathParam("tenantId") String tenantId) {
        return "ping".equals(text) ? "pong" : "";
    }

    @OnClose
    public void onClose(Session session, @PathParam("tenantId") String tenantId) {
        VideoWebSocketSessionHolder.removeDeviceStatusSession(tenantId, session);
        log.info("[WS-设备状态] 连接关闭, sessionId={}, tenantId={}", session.getId(), tenantId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.warn("[WS-设备状态] 连接异常, sessionId={}, error={}", session.getId(), error.getMessage());
    }
}
