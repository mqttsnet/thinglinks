package com.mqttsnet.thinglinks.mqs.bus.protocol.mqtt;

import com.mqttsnet.thinglinks.bus.route.TopicRoute;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import com.mqttsnet.thinglinks.mqs.bus.protocol.AbstractKafkaEdgeAdapter;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.springframework.stereotype.Component;

/**
 * MQTT 控制信令 adapter,SUBSCRIBE / UNSUBSCRIBE 等控制 ack,跳过 CORE 直接 POST。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@TopicRoute(
    value = {
        KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_SUBSCRIPTION_ACKED_TOPIC,
        KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_UNSUBSCRIPTION_ACKED_TOPIC
    },
    mode = MatchModeEnum.EXACT,
    group = DispatchGroupEnum.CONTROL_ACK
)
public class MqttControlAckEdgeAdapter extends AbstractKafkaEdgeAdapter {
    @Override
    public ProtocolTypeEnum supports() {
        return ProtocolTypeEnum.MQTT;
    }
}
