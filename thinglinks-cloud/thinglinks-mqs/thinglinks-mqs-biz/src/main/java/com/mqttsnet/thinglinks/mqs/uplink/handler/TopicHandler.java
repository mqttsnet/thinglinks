package com.mqttsnet.thinglinks.mqtt.handler;

import com.mqttsnet.thinglinks.entity.mqtt.source.MqttMessageEventSource;

/**
 * @program: thinglinks-cloud
 * @description:
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 22:45
 **/
public interface TopicHandler {

    /**
     * 处理MQTT消息事件源
     * <p>
     * 该方法用于处理从MQTT代理接收到的消息事件源。
     * 实现类需要根据事件源中的主题（topic）和服务质量等级（qos）来执行适当的业务逻辑。
     * <p>
     * 实现类可以根据事件源中的其他属性（如payloadBytes）来执行更复杂的业务逻辑。
     *
     * @param eventSource the MQTT message event source.
     */
    void handle(MqttMessageEventSource eventSource);
}
