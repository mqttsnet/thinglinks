package com.mqttsnet.thinglinks.mqs.bus.inbound.kafka;

import java.util.List;

import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.mqs.bus.dispatcher.BusPipelineDispatcher;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * MQTT 协议 Kafka 入站消费者.
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
public class MqttKafkaInboundConsumer extends AbstractProtocolKafkaInboundConsumer {

    private static final String CONSUMER_GROUP = "CID_THINGLINKS_BUS_MQTT";
    private static final String PROTOCOL_NAME = "MQTT";

    public MqttKafkaInboundConsumer(BusPipelineDispatcher dispatcher, BusStatsService statsService) {
        super(dispatcher, statsService);
    }

    @Override
    protected String protocolName() {
        return PROTOCOL_NAME;
    }

    @KafkaListener(
        topics = {
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_CLIENT_CONNECTED_TOPIC,
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_CLIENT_DISCONNECTED_TOPIC,
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_SERVER_CONNECTED_TOPIC,
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_DEVICE_KICKED_TOPIC,
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_SUBSCRIPTION_ACKED_TOPIC,
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_UNSUBSCRIPTION_ACKED_TOPIC,
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_DISTRIBUTION_ERROR_TOPIC,
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_DISTRIBUTION_COMPLETED_TOPIC,
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_PING_REQ_TOPIC
        },
        groupId = CONSUMER_GROUP,
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        handleBatch(records, ack);
    }
}
