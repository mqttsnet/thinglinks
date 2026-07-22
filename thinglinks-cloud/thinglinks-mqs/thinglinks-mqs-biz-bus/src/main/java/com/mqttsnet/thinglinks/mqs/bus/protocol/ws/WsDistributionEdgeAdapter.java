package com.mqttsnet.thinglinks.mqs.bus.protocol.ws;

import com.mqttsnet.thinglinks.bus.route.TopicRoute;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import com.mqttsnet.thinglinks.mqs.bus.protocol.AbstractKafkaEdgeAdapter;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.springframework.stereotype.Component;

/**
 * WebSocket broker 分发失败回执 adapter,DISPATCH_ERROR 只走 POST(跳 CORE).
 *
 * <h3>说明</h3>
 * 成功回执(WS broker 写 websocket.distribution.completed.topic)已被 {@code WsDeviceDataEdgeAdapter} 路由为 PUBLISH 主流程
 * (producer {@code WebSocketDeviceOpenAccessProtocolEndpoint} 直接写完整设备消息 body),
 * 本 adapter 不再消费成功回执 ── 成功统计可由 DEVICE_DATA 主流程的 MetricStage 落地.
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@TopicRoute(
    value = KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_DISTRIBUTION_ERROR_TOPIC,
    mode = MatchModeEnum.EXACT,
    group = DispatchGroupEnum.DISTRIBUTION_ACK
)
public class WsDistributionEdgeAdapter extends AbstractKafkaEdgeAdapter {
    @Override
    public ProtocolTypeEnum supports() {
        return ProtocolTypeEnum.WEBSOCKET;
    }
}
