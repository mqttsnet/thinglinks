package com.mqttsnet.thinglinks.mqs.bus.protocol.tcp;

import com.mqttsnet.thinglinks.bus.route.TopicRoute;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import com.mqttsnet.thinglinks.mqs.bus.protocol.AbstractKafkaEdgeAdapter;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.springframework.stereotype.Component;

/**
 * TCP broker 分发失败回执 adapter,DISPATCH_ERROR 只走 POST(跳 CORE).
 *
 * <h3>说明</h3>
 * 成功回执(TCP broker 写 tcp.distribution.completed.topic)已被 {@code TcpDeviceDataEdgeAdapter} 路由为 PUBLISH 主流程,
 * 本 adapter 不再消费成功回执 ── 成功统计可由 DEVICE_DATA 主流程的 MetricStage 落地.
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@TopicRoute(
    value = KafkaConsumerTopicConstant.Mqs.MqsTcp.THINGLINKS_TCP_DISTRIBUTION_ERROR_TOPIC,
    mode = MatchModeEnum.EXACT,
    group = DispatchGroupEnum.DISTRIBUTION_ACK
)
public class TcpDistributionEdgeAdapter extends AbstractKafkaEdgeAdapter {
    @Override
    public ProtocolTypeEnum supports() {
        return ProtocolTypeEnum.TCP;
    }
}
