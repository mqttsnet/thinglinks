package com.mqttsnet.thinglinks.dto.bridge.protocol.mqtt;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * MQTT 连接参数 DTO（外部 MQTT broker 互联）。
 *
 * @author mqttsnet
 */
public class MqttConnectionDto implements ProtocolConnectionDto {

    /**
     * Broker URL（tcp://host:1883 或 ssl://host:8883）。必填
     */
    public String broker;

    /**
     * Client ID。空则自动生成
     */
    public String clientId;

    /**
     * Topic 模板，支持占位符（如 out/${productId}/${deviceId}）。必填
     */
    public String topicTemplate;

    /**
     * QoS：0 / 1 / 2。默认 1
     */
    public Integer qos;

    /**
     * 是否为保留消息（retained）。默认 false
     */
    public Boolean retained;

    /**
     * MQTT 用户名
     */
    public String username;
}
