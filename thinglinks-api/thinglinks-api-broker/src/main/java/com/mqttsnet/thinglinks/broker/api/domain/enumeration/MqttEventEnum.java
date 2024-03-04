package com.mqttsnet.thinglinks.broker.api.domain.enumeration;


import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @program: thinglinks
 * @description: MQTT事件枚举
 * @packagename: com.mqttsnet.thinglinks.broker.api.domain.enumeration
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-12-16 19:42
 **/
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "MqttEventEnum", description = "MQTT事件枚举")
public enum MqttEventEnum {

    /**
     * Event type for publish.
     */
    PUBLISH("PUBLISH"),

    /**
     * Event type for write.
     */
    WRITE("WRITE"),

    /**
     * Event type for cluster.
     */
    CLUSTER("CLUSTER"),

    /**
     * Event type for connect.
     */
    CONNECT("CONNECT"),

    /**
     * Event type for close.
     */
    CLOSE("CLOSE"),

    /**
     * Event type for subscribe.
     */
    SUBSCRIBE("SUBSCRIBE"),

    /**
     * Event type for unsubscribe.
     */
    UNSUBSCRIBE("UNSUBSCRIBE"),

    /**
     * Event type for bridge.
     */
    BRIDGE("BRIDGE"),

    /**
     * Event type for disconnect.
     */
    DISCONNECT("DISCONNECT"),

    /**
     * Event type for ping.
     */
    PING("PING"),

    /**
     * Event type for publish acknowledgment.
     */
    PUBLISH_ACK("PUBLISH_ACK"),

    /**
     * Event type for retry.
     */
    RETRY("RETRY"),

    /**
     * Event type for heart timeout.
     */
    HEART_TIMEOUT("HEART_TIMEOUT"),

    /**
     * Event type for system.
     */
    SYSTEM("SYSTEM"),

    ;

    private String name;

    /**
     * Retrieves the corresponding MqttEventEnum for a given name.
     *
     * @param name the name of the MQTT event.
     * @return an Optional of MqttEventEnum.
     */
    public static Optional<MqttEventEnum> getMqttEventEnum(String name) {
        if (StringUtils.isEmpty(name)) {
            return Optional.empty();
        }
        return Stream.of(MqttEventEnum.values())
                .filter(event -> event.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
