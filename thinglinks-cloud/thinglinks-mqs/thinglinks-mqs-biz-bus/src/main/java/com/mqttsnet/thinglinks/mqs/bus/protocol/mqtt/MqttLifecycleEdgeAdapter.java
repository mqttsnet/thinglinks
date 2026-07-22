package com.mqttsnet.thinglinks.mqs.bus.protocol.mqtt;

import com.mqttsnet.thinglinks.bus.route.TopicRoute;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import com.mqttsnet.thinglinks.mqs.bus.protocol.AbstractKafkaEdgeAdapter;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.springframework.stereotype.Component;

/**
 * MQTT 生命周期 adapter,承载 CONNECT/DISCONNECT/CLOSE/KICKED/PING,走全套管道。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@TopicRoute(
    value = {
        KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_CLIENT_CONNECTED_TOPIC,
        KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_CLIENT_DISCONNECTED_TOPIC,
        KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_SERVER_CONNECTED_TOPIC,
        KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_DEVICE_KICKED_TOPIC,
        KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_PING_REQ_TOPIC
    },
    mode = MatchModeEnum.EXACT,
    group = DispatchGroupEnum.DEVICE_LIFECYCLE
)
public class MqttLifecycleEdgeAdapter extends AbstractKafkaEdgeAdapter {
    @Override
    public ProtocolTypeEnum supports() {
        return ProtocolTypeEnum.MQTT;
    }
}
