package com.mqttsnet.thinglinks.broker.mqs.consumer.kafka;

import com.mqttsnet.thinglinks.broker.mqs.protocol.ProtocolHandlerFactory;
import com.mqttsnet.thinglinks.broker.mqs.protocol.handler.ProtocolHandler;
import com.mqttsnet.thinglinks.common.core.mqs.ConsumerTopicConstant;
import com.mqttsnet.thinglinks.link.api.domain.product.enumeration.ProtocolTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: Mqtt Message kafka监听消息
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-06-18 11:46
 **/
@Slf4j
@Component
public class MqttMessageKafkaConsumerHandler {

    private final ProtocolHandlerFactory protocolHandlerFactory;

    public MqttMessageKafkaConsumerHandler(ProtocolHandlerFactory protocolHandlerFactory) {
        this.protocolHandlerFactory = protocolHandlerFactory;
    }

    /**
     * 监听kafka消息(批量)
     *
     * @param records kafka的批量消息，用consumerRecord可以接收到更详细的信息，也可以用String message只接收消息
     * @param ack     kafka的消息确认
     */
    @KafkaListener(topics = {ConsumerTopicConstant.Mqtt.THINGLINKS_MQS_MQTT_MSG,
            ConsumerTopicConstant.Mqtt.THINGLINKS_MQTT_CLIENT_CONNECTED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_MQTT_CLIENT_DISCONNECTED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_MQTT_SERVER_CONNECTED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_MQTT_DEVICE_KICKED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_MQTT_SUBSCRIPTION_ACKED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_MQTT_UNSUBSCRIPTION_ACKED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_MQTT_DISTRIBUTION_ERROR_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_MQTT_DISTRIBUTION_COMPLETED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_MQTT_PING_REQ_TOPIC
    }, errorHandler = "myKafkaListenerErrorHandler", containerFactory = "kafkaListenerContainerFactory")
    @KafkaHandler
    public void handleBatchMessages(List<ConsumerRecord<?, ?>> records, Acknowledgment ack) {
        try {
            // 用于测试异常处理
            // int i = 1 / 0;
            log.info("handleBatchMessages Listener, Thread ID:{}, records size:{}", Thread.currentThread().getId(), records.size());
            for (ConsumerRecord<?, ?> record : records) {
                Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                if (!kafkaMessage.isPresent()) {
                    log.error("topic:{},报文体为空或数据格式有误已忽略" + record.topic());
                    return;
                }
                String message = record.value().toString();
                String topic = record.topic();
                log.info("handleBatchMessages--> topic={} Received message={}", topic, message);
                processMessage(message);
            }
        } finally {
            // 手动确认
            ack.acknowledge();
        }
    }

    /**
     * 处理消息
     *
     * @param message 消息记录
     */
    private void processMessage(String message) {
        log.info("ThingLinks物联网平台数据消费-->Received message={}", message);
        try {
            ProtocolHandler handler = protocolHandlerFactory.getHandler(ProtocolTypeEnum.MQTT);
            if (handler != null) {
                handler.processMessage(message);
            } else {
                log.error("未找到对应的协议处理器: " + ProtocolTypeEnum.MQTT);
                throw new IllegalStateException("未找到对应的协议处理器: " + ProtocolTypeEnum.MQTT);
            }
        } catch (Exception e) {
            log.error("ThingLinks物联网平台数据消费-->消费失败，失败原因：{}", e.getMessage());
        }
    }

}
