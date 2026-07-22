package com.mqttsnet.thinglinks.mqs.bus.protocol.tcp;

import com.mqttsnet.thinglinks.bus.route.TopicRoute;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import com.mqttsnet.thinglinks.mqs.bus.protocol.AbstractKafkaEdgeAdapter;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.springframework.stereotype.Component;

/**
 * TCP 设备主数据 adapter ── 走 PRE→CORE→POST 全套管道。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@TopicRoute(
    value = KafkaConsumerTopicConstant.Mqs.MqsTcp.THINGLINKS_TCP_DISTRIBUTION_COMPLETED_TOPIC,
    mode = MatchModeEnum.EXACT,
    group = DispatchGroupEnum.DEVICE_DATA
)
public class TcpDeviceDataEdgeAdapter extends AbstractKafkaEdgeAdapter {
    @Override
    public ProtocolTypeEnum supports() {
        return ProtocolTypeEnum.TCP;
    }
}
