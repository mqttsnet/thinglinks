package com.mqttsnet.thinglinks.mqs.bus.inbound.kafka.audit;

import com.mqttsnet.thinglinks.common.mq.ConsumerGroupConstant;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 客户端认证失败 audit consumer ── 单 record log 消费,不入业务流程.
 *
 * <p>对应 BifroMQ {@code NotAuthorizedClient} 事件;plugin 把事件 JSON 发到
 * {@link KafkaConsumerTopicConstant.Mqs.MqsMqtt#THINGLINKS_MQTT_CLIENT_UNAUTHORIZED_TOPIC},
 * 本 consumer 仅打 INFO 日志便于运维 {@code grep '[audit.unauthorized]'} 排查认证失败设备.
 *
 * <p>失败仅 warn 不抛 + finally ack:audit 是 best-effort,不能阻塞 Kafka offset 提交.
 *
 * @author mqttsnet
 * @since 2026-05-17
 */
@Component
@Slf4j
public class NotAuthorizedClientAuditConsumer {

    private static final String CONSUMER_GROUP = ConsumerGroupConstant.THINGLINKS_CONSUMER_GROUP_PREFIX + "AUDIT_NOT_AUTHORIZED";

    @KafkaListener(
        topics = KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_CLIENT_UNAUTHORIZED_TOPIC,
        groupId = CONSUMER_GROUP,
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            log.info("[audit.unauthorized] partition={} offset={} key={} payload={}",
                record.partition(), record.offset(), record.key(), record.value());
        } catch (Exception e) {
            log.warn("[audit.unauthorized] log failed (non-blocking) offset={}", record.offset(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
