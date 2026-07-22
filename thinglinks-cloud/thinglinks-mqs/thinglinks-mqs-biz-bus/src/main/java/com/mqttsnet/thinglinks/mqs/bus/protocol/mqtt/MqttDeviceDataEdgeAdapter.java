package com.mqttsnet.thinglinks.mqs.bus.protocol.mqtt;

import com.mqttsnet.thinglinks.bus.route.TopicRoute;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import com.mqttsnet.thinglinks.mqs.bus.protocol.AbstractKafkaEdgeAdapter;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.springframework.stereotype.Component;

/**
 * MQTT 设备主数据 adapter,承载 PUBLISH 主数据 topic,走 PRE→CORE→POST 全套管道.
 *
 * <h3>数据源</h3>
 * BifroMQ Standalone 部署下 plugin DISTED 事件 → {@code mqtt.distribution.completed.topic} ──
 * body 带完整 PUBLISH 报文(topic/qos/payload/publisher),是设备上行唯一可用来源.
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@TopicRoute(
    value = KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_DISTRIBUTION_COMPLETED_TOPIC,
    mode = MatchModeEnum.EXACT,
    group = DispatchGroupEnum.DEVICE_DATA
)
public class MqttDeviceDataEdgeAdapter extends AbstractKafkaEdgeAdapter {
    @Override
    public ProtocolTypeEnum supports() {
        return ProtocolTypeEnum.MQTT;
    }
}
