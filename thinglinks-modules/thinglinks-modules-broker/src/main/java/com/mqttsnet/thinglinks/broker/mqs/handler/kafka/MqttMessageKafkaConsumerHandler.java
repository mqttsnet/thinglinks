package com.mqttsnet.thinglinks.broker.mqs.handler.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.broker.api.domain.enumeration.MqttEventEnum;
import com.mqttsnet.thinglinks.broker.mqs.event.publisher.MqttEventPublisher;
import com.mqttsnet.thinglinks.common.core.mqs.ConsumerTopicConstant;
import com.mqttsnet.thinglinks.link.common.cache.helper.CacheDataHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: Mqtt Message kafka监听消息
 * @packagename: com.mqttsnet.thinglinks.kafka
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-06-18 11:46
 **/
@Slf4j
@Component
public class MqttMessageKafkaConsumerHandler {

    @Autowired
    private MqttEventPublisher eventPublisher;

    @Autowired
    private CacheDataHelper cacheDataHelper;

    /**
     * 监听kafka消息(批量)
     *
     * @param records kafka的批量消息，用consumerRecord可以接收到更详细的信息，也可以用String message只接收消息
     * @param ack     kafka的消息确认
     */
    @KafkaListener(topics = {ConsumerTopicConstant.Mqtt.THINGLINKS_MQS_MQTT_MSG,
            ConsumerTopicConstant.Mqtt.THINGLINKS_CLIENT_CONNECTED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_CLIENT_DISCONNECTED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_SERVER_CONNECTED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_DEVICE_KICKED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_SUBSCRIPTION_ACKED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_UNSUBSCRIPTION_ACKED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_DISTRIBUTION_ERROR_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_DISTRIBUTION_COMPLETED_TOPIC,
            ConsumerTopicConstant.Mqtt.THINGLINKS_PING_REQ_TOPIC
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
            JSONObject thinglinksMessage = JSON.parseObject(String.valueOf(message));
            String eventStr = Optional.ofNullable(thinglinksMessage.getString("event"))
                    .orElse("");
            Long tenantId = Optional.ofNullable(thinglinksMessage.getString("tenantId"))
                    .filter(StringUtils::isNotBlank)
                    .map(Long::valueOf)
                    .orElse(null);
            if (StringUtils.isEmpty(eventStr) || tenantId == null) {
                log.warn("event or tenantId cannot be empty {}", eventStr);
                return;
            }
            Optional<MqttEventEnum> optionalEvent = MqttEventEnum.getMqttEventEnum(thinglinksMessage.get("event").toString());
            optionalEvent.ifPresent(event -> {
                switch (event) {
                    case CONNECT:
                        eventPublisher.publishMqttConnectEvent(MqttEventEnum.CONNECT, thinglinksMessage.toJSONString());
                        break;
                    case CLOSE:
                        eventPublisher.publishMqttCloseEvent(MqttEventEnum.CLOSE, thinglinksMessage.toJSONString());
                        break;
                    case DISCONNECT:
                        eventPublisher.publishMqttDisconnectEvent(MqttEventEnum.DISCONNECT, thinglinksMessage.toJSONString());
                        break;
                    case PUBLISH:
                        eventPublisher.publishMqttPublishEvent(MqttEventEnum.PUBLISH, thinglinksMessage.toJSONString());
                        break;
                    case SUBSCRIBE:
                        eventPublisher.publishMqttSubscribeEvent(MqttEventEnum.SUBSCRIBE, thinglinksMessage.toJSONString());
                        break;
                    case UNSUBSCRIBE:
                        eventPublisher.publishMqttUnsubscribeEvent(MqttEventEnum.UNSUBSCRIBE, thinglinksMessage.toJSONString());
                        break;
                    case PING:
                        eventPublisher.publishMqttPingEvent(MqttEventEnum.PING, thinglinksMessage.toJSONString());
                        break;
                    default:
                        break;
                }
            });
        } catch (Exception e) {
            log.error("ThingLinks物联网平台数据消费-->消费失败，失败原因：{}", e.getMessage());
        }
    }

}
