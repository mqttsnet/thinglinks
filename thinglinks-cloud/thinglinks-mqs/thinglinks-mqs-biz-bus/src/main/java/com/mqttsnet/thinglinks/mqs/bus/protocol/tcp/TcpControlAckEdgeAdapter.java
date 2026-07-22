package com.mqttsnet.thinglinks.mqs.bus.protocol.tcp;

import com.mqttsnet.thinglinks.bus.route.TopicRoute;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import com.mqttsnet.thinglinks.mqs.bus.protocol.AbstractKafkaEdgeAdapter;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.springframework.stereotype.Component;

/**
 * TCP 控制信令 adapter ── SUBSCRIBE/UNSUBSCRIBE,跳过 CORE。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@TopicRoute(
    value = {
        KafkaConsumerTopicConstant.Mqs.MqsTcp.THINGLINKS_TCP_SUBSCRIPTION_ACKED_TOPIC,
        KafkaConsumerTopicConstant.Mqs.MqsTcp.THINGLINKS_TCP_UNSUBSCRIPTION_ACKED_TOPIC
    },
    mode = MatchModeEnum.EXACT,
    group = DispatchGroupEnum.CONTROL_ACK
)
public class TcpControlAckEdgeAdapter extends AbstractKafkaEdgeAdapter {
    @Override
    public ProtocolTypeEnum supports() {
        return ProtocolTypeEnum.TCP;
    }
}
