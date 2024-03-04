package com.mqttsnet.thinglinks.link.api.domain.device.enumeration;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 设备动作类型 枚举
 * </p>
 *
 * @author shihuan sun
 * @date 2023-08-20
 */
@Getter
@NoArgsConstructor
@ApiModel(value = "DeviceActionTypeEnum", description = "设备动作类型 枚举")
public enum DeviceActionTypeEnum {

    PUBLISH("PUBLISH", "Publishing data to a topic."),
    WRITE("WRITE", "Writing data to a device or topic."),

    CLUSTER("CLUSTER", "Cluster-based actions or references to clusters."),
    CONNECT("CONNECT", "Initiating a connection to a server."),
    CLOSE("CLOSE", "Closing a connection to a server."),

    SUBSCRIBE("SUBSCRIBE", "Subscribing to a topic to receive messages."),

    UNSUBSCRIBE("UNSUBSCRIBE", "Unsubscribing from a topic to stop receiving messages."),

    BRIDGE("BRIDGE", "Bridging or connecting two different networks or brokers."),
    DISCONNECT("DISCONNECT", "Terminating a connection gracefully."),
    PING("PING", "Sending a ping request to maintain or check a connection."),

    PUBLISH_ACK("PUBLISH_ACK", "Acknowledging the receipt of a published message."),

    RETRY("RETRY", "Retrying a certain action or request after a failure."),

    HEART_TIMEOUT("HEART_TIMEOUT", "An event indicating a timeout due to lack of heartbeat or keep-alive signal."),

    SYSTEM("SYSTEM", "System level or internal events."),

    UNKNOWN("UNKNOWN", "Unknown action type.");

    private String action;
    private String description;

    DeviceActionTypeEnum(String action, String description) {
        this.action = action;
        this.description = description;
    }


}
