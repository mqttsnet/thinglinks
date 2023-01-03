package com.mqttsnet.thinglinks.link.common.rockermq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.enums.MqttEvent;
import com.mqttsnet.thinglinks.common.rocketmq.constant.ConsumerGroupConstant;
import com.mqttsnet.thinglinks.common.rocketmq.constant.ConsumerTopicConstant;
import com.mqttsnet.thinglinks.link.service.device.DeviceActionService;
import com.mqttsnet.thinglinks.link.service.device.DeviceDatasService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Description: Mqtt动作消息消费（Rocketmq模式）
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/11/22$ 16:11$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/22$ 16:11$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = ConsumerGroupConstant.THINGLINKS_BROKER_GROUP, topic = ConsumerTopicConstant.THINGLINKS_LINK_MQTT_MSG, messageModel = MessageModel.CLUSTERING)
public class DeviceActionMessageConsumer implements RocketMQListener {
    @Autowired
    private DeviceActionService deviceActionService;
    @Autowired
    private DeviceDatasService deviceDatasService;

    @Async("linkAsync")
    @Override
    public void onMessage(Object message) {
        assert message != null : "message cannot be empty";
        log.info("ThingLinks物联网平台数据消费-->Received message={}", message);
        JSONObject thinglinksMessage = JSON.parseObject(String.valueOf(message));
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
