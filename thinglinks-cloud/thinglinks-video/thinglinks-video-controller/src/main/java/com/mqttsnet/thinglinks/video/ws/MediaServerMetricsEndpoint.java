package com.mqttsnet.thinglinks.video.ws;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.utils.SpringUtils;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.common.ws.WebSocketAuthGuard;
import com.mqttsnet.thinglinks.common.ws.WebSocketAuthHeaderCaptor;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerMetricsResultVO;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

/**
 * 流媒体服务器实时性能指标 WebSocket 端点 ── 每 5 秒推送 CPU/内存/流数/吞吐;
 * onOpen 校验 path tenantId == 登录 tenantId;ZLM/ABL 兼容由 service 自动路由。
 *
 * @author mqttsnet
 */
@ServerEndpoint(
        value = "/anyone/videoSocket/mediaServerMetrics/{tenantId}/{serverId}",
        configurator = WebSocketAuthHeaderCaptor.class
)
@Component
@Slf4j
public class MediaServerMetricsEndpoint {

    /**
     * 推送间隔（毫秒）
     */
    private static final long PUSH_INTERVAL_MS = 5000;

    /**
     * 活跃会话的定时推送任务（key = sessionId）
     */
    private static final Map<String, ScheduledFuture<?>> SCHEDULED_TASKS = new ConcurrentHashMap<>();

    /**
     * WebSocket 连接建立时启动定时推送任务。
     *
     * <p>鉴权:onOpen 校验 path tenantId == 登录 tenantId,防止跨租户拉取媒体服务器指标。</p>
     *
     * @param session  WebSocket 会话
     * @param config   endpoint 配置(从中读 {@link WebSocketAuthHeaderCaptor} 捕获的 header)
     * @param tenantId 租户 ID
     * @param serverId 流媒体服务器 ID
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config,
                       @PathParam("tenantId") String tenantId,
                       @PathParam("serverId") String serverId) {
        if (!WebSocketAuthGuard.requireSameTenant(session, config, tenantId)) {
            return;
        }
        log.info("[WS-媒体指标] 连接建立, sessionId={}, tenantId={}, serverId={}", session.getId(), tenantId, serverId);

        TaskScheduler scheduler = SpringUtils.getBean(TaskScheduler.class);
        VideoMediaServerService service = SpringUtils.getBean(VideoMediaServerService.class);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            if (!session.isOpen()) {
                cancelTask(session.getId());
                return;
            }
            try {
                ContextUtil.setTenantId(tenantId);
                VideoMediaServerMetricsResultVO metrics = service.getRealTimeMetrics(Long.valueOf(serverId));
                session.getBasicRemote().sendText(JsonUtil.toJson(metrics));
            } catch (Exception e) {
                log.debug("[WS-媒体指标] 推送失败: sessionId={}, error={}", session.getId(), e.getMessage());
            }
        }, PUSH_INTERVAL_MS);

        SCHEDULED_TASKS.put(session.getId(), future);
    }

    /**
     * 接收客户端消息（支持 ping 心跳保活）。
     *
     * @param text     消息内容
     * @param tenantId 租户 ID
     * @param serverId 流媒体服务器 ID
     * @return 响应消息
     */
    @OnMessage
    public String onMsg(String text,
                        @PathParam("tenantId") String tenantId,
                        @PathParam("serverId") String serverId) {
        if ("ping".equals(text)) {
            return "pong";
        }
        return StrPool.EMPTY;
    }

    /**
     * WebSocket 连接关闭时取消定时推送任务。
     *
     * @param session  WebSocket 会话
     * @param tenantId 租户 ID
     * @param serverId 流媒体服务器 ID
     */
    @OnClose
    public void onClose(Session session,
                        @PathParam("tenantId") String tenantId,
                        @PathParam("serverId") String serverId) {
        log.info("[WS-媒体指标] 连接关闭, sessionId={}, tenantId={}, serverId={}", session.getId(), tenantId, serverId);
        cancelTask(session.getId());
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            log.debug("[WS-媒体指标] 关闭会话异常: {}", e.getMessage());
        }
    }

    /**
     * WebSocket 错误处理。
     *
     * @param session WebSocket 会话
     * @param error   异常
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.warn("[WS-媒体指标] 连接异常, sessionId={}, error={}", session.getId(), error.getMessage());
        cancelTask(session.getId());
    }

    /**
     * 取消指定会话的定时推送任务。
     *
     * @param sessionId 会话 ID
     */
    private static void cancelTask(String sessionId) {
        ScheduledFuture<?> future = SCHEDULED_TASKS.remove(sessionId);
        if (future != null) {
            future.cancel(true);
        }
    }
}
