package com.mqttsnet.thinglinks.link.common.consumer.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.enums.MqttEvent;
import com.mqttsnet.thinglinks.common.rocketmq.constant.ConsumerGroupConstant;
import com.mqttsnet.thinglinks.common.kafka.constant.ConsumerTopicConstant;
import com.mqttsnet.thinglinks.link.service.device.DeviceActionService;
import com.mqttsnet.thinglinks.link.service.device.DeviceDatasService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Description: Mqtt动作消息消费（kafka模式）
 * @Author: menffy
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2023/08/08
 * @UpdateUser: menffy
 * @UpdateDate: 2023/08/08
 * @UpdateRemark: https://mqttsnet.yuque.com/gt6zkc/thinglinks/yhzgnpduyo8adxgs
 * @Version: 1.0
 */
@Slf4j
//@Component
public class DeviceActionMessageKafkaConsumer {
    @Autowired
    private DeviceActionService deviceActionService;
    @Autowired
    private DeviceDatasService deviceDatasService;

    @Async("linkAsync")
    @KafkaListener(topics = {
            ConsumerTopicConstant.THINGLINKS_LINK_MQTT_MSG,
            ConsumerTopicConstant.THINGLINKS_CLIENT_CONNECTED_TOPIC,
            ConsumerTopicConstant.THINGLINKS_CLIENT_DISCONNECTED_TOPIC,
            ConsumerTopicConstant.THINGLINKS_SERVER_CONNECTED_TOPIC,
            ConsumerTopicConstant.THINGLINKS_DEVICE_KICKED_TOPIC,
            ConsumerTopicConstant.THINGLINKS_SUBSCRIPTION_ACKED_TOPIC,
            ConsumerTopicConstant.THINGLINKS_UNSUBSCRIPTION_ACKED_TOPIC,
            ConsumerTopicConstant.THINGLINKS_DISTRIBUTION_ERROR_TOPIC,
            ConsumerTopicConstant.THINGLINKS_DISTRIBUTION_COMPLETED_TOPIC})
    public void onMessage(ConsumerRecord<?, ?> record) {
        if (null == record) {
            log.warn("message cannot be empty {}", record);
            return;
        }
        log.info("ThingLinks物联网平台数据消费-->Received message={}", record);
        JSONObject thinglinksMessage = JSON.parseObject(String.valueOf(record.value()));
        try {
            MqttEvent event = MqttEvent.getMqttEventEnum(thinglinksMessage.get("event").toString());
            switch (Objects.requireNonNull(event)) {
                case CONNECT:
                    deviceActionService.insertEvent(thinglinksMessage);
                    deviceActionService.connectEvent(thinglinksMessage);
                    break;
                case CLOSE:
                case DISCONNECT:
                    deviceActionService.insertEvent(thinglinksMessage);
                    deviceActionService.closeEvent(thinglinksMessage);
                    break;
                case PUBLISH:
                    deviceDatasService.insertBaseDatas(thinglinksMessage);
                    break;
                case SUBSCRIBE:
                    deviceActionService.insertEvent(thinglinksMessage);
                    break;
                case UNSUBSCRIBE:
                    deviceActionService.insertEvent(thinglinksMessage);
                    break;
                case PING:
                    deviceActionService.refreshDeviceCache(thinglinksMessage);
                    break;
                default:
            }
        } catch (Exception e) {
            log.error("ThingLinks物联网平台数据消费-->消费失败，失败原因：{}", e.getMessage());
        }
    }
}
