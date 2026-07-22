package com.mqttsnet.thinglinks.mqs.bus.protocol.ws;

import com.mqttsnet.thinglinks.bus.route.TopicRoute;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import com.mqttsnet.thinglinks.mqs.bus.protocol.AbstractKafkaEdgeAdapter;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.springframework.stereotype.Component;

/**
 * WebSocket 生命周期 adapter,CONNECT/DISCONNECT/CLOSE/KICKED/PING,走全套管道。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@TopicRoute(
    value = {
        KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_CLIENT_CONNECTED_TOPIC,
        KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_CLIENT_DISCONNECTED_TOPIC,
        KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_SERVER_DISCONNECTED_TOPIC,
        KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_DEVICE_KICKED_TOPIC,
        KafkaConsumerTopicConstant.Mqs.MqsWebSocket.THINGLINKS_WEBSOCKET_PING_REQ_TOPIC
    },
    mode = MatchModeEnum.EXACT,
    group = DispatchGroupEnum.DEVICE_LIFECYCLE
)
public class WsLifecycleEdgeAdapter extends AbstractKafkaEdgeAdapter {
    @Override
    public ProtocolTypeEnum supports() {
        return ProtocolTypeEnum.WEBSOCKET;
    }
}
