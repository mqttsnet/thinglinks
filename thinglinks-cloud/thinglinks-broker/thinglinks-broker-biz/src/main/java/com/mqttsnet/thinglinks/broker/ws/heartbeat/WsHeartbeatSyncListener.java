package com.mqttsnet.thinglinks.broker.ws.heartbeat;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.broker.ws.session.WebSocketSubject;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.entity.ws.heartbeat.WsHeartbeatSyncEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * WS 心跳跨节点同步监听器(broker 之间互广播)。
 *
 * <h3>消费策略</h3>
 * <ul>
 *   <li>BROADCASTING:每个 broker 节点都收到同一份事件</li>
 *   <li>仅当本节点持有 clientId 的 session(重连漂移场景)才更新本地 lastActiveTime;
 *       不区分是否自己发的 —— 发送方再 touch 一次也只是同值幂等,无害</li>
 * </ul>
 *
 * <h3>典型场景</h3>
 * 多 broker 副本下,设备从 broker-A 重连到 broker-B,但 Redis session 信息还指向 broker-A(短暂窗口);
 * broker-A 看到广播事件后能立即知道:
 * <ul>
 *   <li>本节点持有的旧 session 仍然活跃 → 更新 lastActiveTime(避免 timeout checker 误关)</li>
 *   <li>或者本节点没 session → 跳过</li>
 * </ul>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
@RocketMQMessageListener(
        topic = BizMqRouteConstant.WebSocket.HEARTBEAT_SYNC,
        consumerGroup = BizMqRouteConstant.Groups.WS_HEARTBEAT_SYNC_PREFIX
                + "${spring.application.name}",
        selectorExpression = "*",
        messageModel = MessageModel.BROADCASTING,
        consumeMode = ConsumeMode.CONCURRENTLY
)
public class WsHeartbeatSyncListener implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt message) {
        if (message == null || message.getBody() == null || message.getBody().length == 0) {
            return;
        }
        try {
            WsHeartbeatSyncEvent event = JSON.parseObject(message.getBody(), WsHeartbeatSyncEvent.class);
            if (event == null || event.getClientId() == null) {
                return;
            }

            // 本节点持有 session 才更新(重连漂移场景兜底)
            WebSocketSubject subject = WebSocketSubject.Holder.getSubject().get(event.getClientId());
            if (subject == null) {
                return;
            }
            subject.touchLastActiveTime(event.getTimestamp() == null ? System.currentTimeMillis() : event.getTimestamp());
            // 同步成功路径不打日志(高频事件)
        } catch (Exception e) {
            log.error("[ws-hb-sync] handle failed msgId={} cause={}",
                    message.getMsgId(), e.getMessage(), e);
            // BROADCASTING 模式异常吞掉,避免拖垮 consumer
        }
    }
}
