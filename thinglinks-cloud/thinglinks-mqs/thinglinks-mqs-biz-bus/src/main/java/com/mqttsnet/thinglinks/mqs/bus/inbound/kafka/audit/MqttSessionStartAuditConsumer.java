package com.mqttsnet.thinglinks.mqs.bus.inbound.kafka.audit;

import com.mqttsnet.thinglinks.common.mq.ConsumerGroupConstant;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * MQTT session 创建 audit consumer ── 单 record log 消费,不入业务流程.
 *
 * <p>对应 BifroMQ {@code MQTTSessionStart} 事件;运维 {@code grep '[audit.session.start]'}
 * 可看到所有 session 创建活动(transient / persistent),与 lifecycle CLOSE 配合做会话生命周期审计.
 *
 * <p>失败仅 warn 不抛 + finally ack:audit 是 best-effort,不能阻塞 Kafka offset 提交.
 *
 * @author mqttsnet
 * @since 2026-05-17
 */
@Component
@Slf4j
public class MqttSessionStartAuditConsumer {

    private static final String CONSUMER_GROUP = ConsumerGroupConstant.THINGLINKS_CONSUMER_GROUP_PREFIX + "AUDIT_SESSION_START";

    @KafkaListener(
        topics = KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_SESSION_START_TOPIC,
        groupId = CONSUMER_GROUP,
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            log.info("[audit.session.start] partition={} offset={} key={} payload={}",
                record.partition(), record.offset(), record.key(), record.value());
        } catch (Exception e) {
            log.warn("[audit.session.start] log failed (non-blocking) offset={}", record.offset(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
