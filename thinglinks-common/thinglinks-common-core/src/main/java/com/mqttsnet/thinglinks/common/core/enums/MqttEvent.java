package com.mqttsnet.thinglinks.common.core.enums;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;

/**
 * @program: thinglinks
 * @description: MQTT事件枚举
 * @packagename: com.mqttsnet.thinglinks.common.core.enums
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-12-16 19:42
 **/
public enum MqttEvent {

    PUBLISH("PUBLISH"),
    WRITE("WRITE"),

    CLUSTER("CLUSTER"),
    CONNECT("CONNECT"),
    CLOSE("CLOSE"),

    SUBSCRIBE("SUBSCRIBE"),

    UNSUBSCRIBE("UNSUBSCRIBE"),

    BRIDGE("BRIDGE"),
    DISCONNECT("DISCONNECT"),
    PING("PING"),

    PUBLISH_ACK("PUBLISH_ACK"),

    RETRY("RETRY"),

    HEART_TIMEOUT("HEART_TIMEOUT"),

    SYSTEM("SYSTEM"),

    ;


    private final String name;

    MqttEvent(String name) {
        this.name = name;
    }

    public static MqttEvent getMqttEventEnum(String name) {
        if (StringUtils.isEmpty(name)) return null;
        switch (name.toUpperCase()) {
            case "PUBLISH":
                return PUBLISH;
            case "WRITE":
                return WRITE;
            case "CLUSTER":
                return CLUSTER;
            case "CONNECT":
                return CONNECT;
            case "CLOSE":
                return CLOSE;
            case "SUBSCRIBE":
                return SUBSCRIBE;
            case "UNSUBSCRIBE":
                return UNSUBSCRIBE;
            case "BRIDGE":
                return BRIDGE;
            case "DISCONNECT":
                return DISCONNECT;
            case "PING":
                return PING;
            case "PUBLISH_ACK":
                return PUBLISH_ACK;
            case "RETRY":
                return RETRY;
            case "HEART_TIMEOUT":
                return HEART_TIMEOUT;
            case "SYSTEM":
                return SYSTEM;
            default:
                return null;
        }
    }
}
