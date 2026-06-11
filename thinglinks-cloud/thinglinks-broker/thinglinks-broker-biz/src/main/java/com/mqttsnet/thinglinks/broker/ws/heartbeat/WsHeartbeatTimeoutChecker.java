package com.mqttsnet.thinglinks.broker.ws.heartbeat;

import com.mqttsnet.thinglinks.broker.common.session.WsDeviceSessionRegistry;
import com.mqttsnet.thinglinks.broker.ws.session.WebSocketSubject;
import com.mqttsnet.thinglinks.common.cache.broker.ws.WsDeviceSessionInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WS 心跳超时检测器 + 在线续期器。
 *
 * <h3>职责</h3>
 * 节点本地起一个 {@link ScheduledExecutorService},按 {@code thinglinks.ws.heartbeat.check-interval-ms}
 * 周期(默认 30s)扫本地所有 ws session:
 * <ul>
 *   <li><b>超时关闭</b>:{@code now - lastActiveTime > timeoutMs} 即视为掉线,关闭其 socket
 *       (close 后由 jakarta.websocket 回调 @OnClose 统一清理:删 Redis session 信息 + 投 disconnect)</li>
 *   <li><b>在线续期</b>:未超时的会话把 {@code WsDeviceSessionInfo} 重写进 Redis(刷 90s TTL + 自愈缺失),
 *       使"本地持有 session ⟺ Redis 有 session 信息"始终成立,不依赖设备是否发上行 ── 这样连着但不发
 *       报文的设备也能被下行(在线校验)正确判定为在线</li>
 * </ul>
 *
 * <h3>不依赖 xxl-job</h3>
 * 仅本节点扫本节点的 session,不是分布式调度任务。多节点 broker 各自跑各自的 checker,无重复。
 *
 * <h3>与 WsDeviceSessionRegistry 关系</h3>
 * checker 关 socket 后,session 的 @OnClose 回调会调 {@code WsDeviceSessionRegistry.remove}
 * 自动清除 Redis session 信息(已在 endpoint 实现)。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsHeartbeatTimeoutChecker {

    private final WsDeviceSessionRegistry wsDeviceSessionRegistry;

    /**
     * 心跳超时阈值(ms),默认 90s ── 设备 30s 一次 PING,3 次失败容忍度
     */
    @Value("${thinglinks.ws.heartbeat.timeout-ms:90000}")
    private long timeoutMs;

    /**
     * 扫描周期(ms),默认 30s
     */
    @Value("${thinglinks.ws.heartbeat.check-interval-ms:30000}")
    private long checkIntervalMs;

    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ws-heartbeat-checker");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::checkOnce, checkIntervalMs, checkIntervalMs, TimeUnit.MILLISECONDS);
        log.info("[ws-hb-checker] started timeoutMs={} intervalMs={}", timeoutMs, checkIntervalMs);
    }

    @PreDestroy
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            log.info("[ws-hb-checker] stopped");
        }
    }

    /**
     * 单次扫描 ── 遍历本节点所有 ws session,关闭超时 session。
     *
     * <h3>关闭流程</h3>
     * <ol>
     *   <li>{@link WebSocketSubject#closeAllSessions} 调 {@code session.close(CloseReason)}</li>
     *   <li>jakarta.websocket 容器自动回调 endpoint @OnClose</li>
     *   <li>endpoint @OnClose 清除 Redis session 信息 + 投 Kafka disconnect 事件</li>
     *   <li>本方法不主动 remove sessions Map(由 endpoint @OnClose 内部统一 removeSubject)</li>
     * </ol>
     */
    void checkOnce() {
        Map<String, WebSocketSubject> sessions = WebSocketSubject.Holder.getSubject();
        if (sessions.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        int closed = 0;
        try {
            for (Map.Entry<String, WebSocketSubject> e : sessions.entrySet()) {
                WebSocketSubject subject = e.getValue();
                long idle = now - subject.getLastActiveTime();
                if (idle <= timeoutMs) {
                    // 未超时(在线)→ 续期:把本地会话信息重写进 Redis(刷 90s TTL + 自愈缺失),
                    // 使"本地持有 session ⟺ Redis 有 session 信息"始终成立,不依赖设备是否发上行
                    refreshRedisSession(subject);
                    continue;
                }
                String clientId = e.getKey();
                log.info("[ws-hb-checker] timeout clientId={} idleMs={}", clientId, idle);
                try {
                    // 触发 session.close → @OnClose 自动跑清理流程(remove session 信息 + Kafka disconnect)
                    subject.closeAllSessions(new CloseReason(
                            CloseReason.CloseCodes.GOING_AWAY,
                            "heartbeat timeout " + idle + "ms"));
                    closed++;
                } catch (Exception ex) {
                    log.error("[ws-hb-checker] close failed clientId={} cause={}",
                            clientId, ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            log.error("[ws-hb-checker] scan failed cause={}", e.getMessage(), e);
        }
        if (closed > 0) {
            log.info("[ws-hb-checker] closed {} timeout sessions", closed);
        }
    }

    /**
     * 在线续期 ── 把该 subject 持有的每条会话的 {@link WsDeviceSessionInfo} 重写进 Redis(刷 90s TTL + 自愈缺失)。
     * <p>会话信息存在 jakarta session 的 userProperties 里(@OnOpen 鉴权通过时写入);
     * 顺带把 Redis 里的 lastActiveTime 对齐到本地真实活跃时间。
     */
    private void refreshRedisSession(WebSocketSubject subject) {
        for (Session s : subject.getSessions()) {
            WsDeviceSessionInfo ctx = (WsDeviceSessionInfo) s.getUserProperties().get(WsDeviceSessionInfo.SESSION_KEY);
            if (ctx != null) {
                ctx.setLastActiveTime(subject.getLastActiveTime());
                wsDeviceSessionRegistry.save(ctx);
            }
        }
    }
}
