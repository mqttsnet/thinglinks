package com.mqttsnet.thinglinks.broker.ws.command;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.broker.ws.service.impl.WebSocketBrokerServiceImpl;
import com.mqttsnet.thinglinks.broker.ws.session.WebSocketSubject;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.entity.ws.command.WsCommandBroadcastEvent;
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
 * WS 下行命令广播监听器(broker 全节点)。
 *
 * <h3>消费策略</h3>
 * <ul>
 *   <li>BROADCASTING:每个 broker 节点都收到同一份下行命令事件</li>
 *   <li>查本地 {@link WebSocketSubject.Holder}:命中(即持有该设备 TCP 的那一个节点)才推 socket,其余节点静默忽略 —— 恰好一个节点投递</li>
 * </ul>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
@RocketMQMessageListener(
    topic = BizMqRouteConstant.WebSocket.COMMAND_DOWNLINK,
    consumerGroup = BizMqRouteConstant.Groups.WS_COMMAND_DOWNLINK_PREFIX
        + "${spring.application.name}",
    selectorExpression = "*",
    messageModel = MessageModel.BROADCASTING,
    consumeMode = ConsumeMode.CONCURRENTLY
)
public class WsCommandDownlinkListener implements RocketMQListener<MessageExt> {

    private final WebSocketBrokerServiceImpl webSocketBrokerService;

    @Override
    public void onMessage(MessageExt message) {
        if (message == null || message.getBody() == null || message.getBody().length == 0) {
            return;
        }
        try {
            WsCommandBroadcastEvent event = JSON.parseObject(message.getBody(), WsCommandBroadcastEvent.class);
            if (event == null || event.getClientId() == null || event.getEncodedMessage() == null) {
                return;
            }
            String clientId = event.getClientId();

            // 本节点持有该设备 session 才投递(否则忽略 —— 广播下绝大多数节点都不持有)
            WebSocketSubject subject = WebSocketSubject.Holder.getSubject().get(clientId);
            if (subject == null) {
                return;
            }
            webSocketBrokerService.publishLocal(clientId, event.getEncodedMessage());
        } catch (Exception e) {
            // BROADCASTING 模式异常吞掉,避免拖垮 consumer;命令由设备重试 / 业务重发兜底
            log.warn("[ws-command-downlink] handle failed msgId={} cause={}", message.getMsgId(), e.getMessage());
        }
    }
}
