package com.mqttsnet.thinglinks.broker.ws.heartbeat;

import com.mqttsnet.basic.rocketmq.producer.RocketmqTemplate;
import com.mqttsnet.thinglinks.broker.ws.session.WebSocketSubject;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.entity.ws.heartbeat.WsHeartbeatSyncEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * WS 设备心跳跟踪器(设备任意上行报文都会调一次 {@link #update})。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>刷新本地 {@link WebSocketSubject#touchLastActiveTime} ── 给 timeout checker 判活用(节点内存级)</li>
 *   <li>RocketMQ broadcast ── 让其它 broker 节点同步本地 lastActiveTime(设备重连漂移兜底)</li>
 * </ul>
 *
 * <p><b>Redis 侧的 session 信息续期不在这里</b> —— 它在端点 PING 分支由
 * {@code WsDeviceSessionRegistry.save} 完成(PING 节奏 + 自愈),避免每条上行都写 Redis。
 *
 * <h3>故障容忍</h3>
 * RocketMQ 失败仅 warn 不抛 ── 心跳是软实时,不应阻塞设备上行处理路径。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsHeartbeatTracker {

    private final ObjectProvider<RocketmqTemplate> rocketmqTemplateProvider;

    /**
     * 设备上行报文时调用 ── 持有 session 的 broker 节点(且仅此节点)调用本方法。
     *
     * @param tenantId 租户 ID
     * @param clientId 设备 clientId
     */
    public void update(Long tenantId, String clientId) {
        long now = System.currentTimeMillis();

        // 1. 本地 session lastActiveTime(timeout checker 据此判活)
        WebSocketSubject subject = WebSocketSubject.Holder.getSubject().get(clientId);
        if (subject != null) {
            subject.touchLastActiveTime(now);
        }

        // 2. 跨节点广播(其它 broker 节点同步本地 lastActiveTime,重连漂移兜底)
        try {
            RocketmqTemplate rocketmqTemplate = rocketmqTemplateProvider.getIfAvailable();
            if (rocketmqTemplate != null) {
                WsHeartbeatSyncEvent event = WsHeartbeatSyncEvent.builder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .timestamp(now)
                    .build();
                rocketmqTemplate.asyncSend(BizMqRouteConstant.WebSocket.HEARTBEAT_SYNC, event,
                    new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sr) {
                            // 成功路径不打日志(高频事件,日志会爆)
                        }

                        @Override
                        public void onException(Throwable ex) {
                            log.warn("[ws-hb] sync broadcast failed (non-blocking) cause={}", ex.getMessage());
                        }
                    });
            }
        } catch (Exception e) {
            log.warn("[ws-hb] broadcast prepare failed (non-blocking) clientId={} cause={}", clientId, e.getMessage());
        }
    }
}
