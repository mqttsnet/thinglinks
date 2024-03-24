package com.mqttsnet.thinglinks.broker.mqs.consumer.rocketmq;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.broker.api.domain.enumeration.MqttEventEnum;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.event.publisher.MqttEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @Description: Mqtt Message Rocketmq模式 消费者
 * @ClassName: MqttMessageRocketmqConsumerHandler
 * @Date: 2023/04/28$ 16:11$
 * @author: ShiHuan Sun
 */
@Slf4j
//@Component
//@RocketMQMessageListener(consumerGroup = ConsumerGroupConstant.THINGLINKS_GROUP, topic = ConsumerTopicConstant.Mqtt.THINGLINKS_MQS_MQTT_MSG, messageModel = MessageModel.CLUSTERING)
public class MqttMessageRocketmqConsumerHandler implements RocketMQListener {

    @Resource
    private MqttEventPublisher eventPublisher;

    @Async("mqsAsync-mqttMsg")
    @Override
    public void onMessage(Object message) {
        if (null == message) {
            log.warn("message cannot be empty {}", message);
            return;
        }
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
