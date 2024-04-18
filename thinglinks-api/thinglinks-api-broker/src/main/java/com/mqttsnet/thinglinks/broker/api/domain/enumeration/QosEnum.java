package com.mqttsnet.thinglinks.broker.api.domain.enumeration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * -----------------------------------------------------------------------------
 * File Name: QosEnum.java
 * -----------------------------------------------------------------------------
 * Description:
 * Enumeration for MQTT Quality of Service (QoS) levels
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-11-11 16:53
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "QosEnum", description = "Enumeration for MQTT Quality of Service (QoS) levels")
public enum QosEnum {

    /**
     * QoS level 0 - At most once delivery. The message is delivered according to the capabilities of the underlying network.
     * No response is sent by the receiver and no retry is performed by the sender. The message arrives at the receiver either once or not at all.
     */
    @ApiModelProperty(value = "At most once delivery")
    AT_MOST_ONCE(0, "At most once"),

    /**
     * QoS level 1 - At least once delivery. This quality of service ensures that the message arrives at the receiver at least once.
     * A QoS 1 PUBLISH Packet has a Packet Identifier in its variable header and is acknowledged by a PUBACK Packet.
     */
    @ApiModelProperty(value = "At least once delivery")
    AT_LEAST_ONCE(1, "At least once"),

    /**
     * QoS level 2 - Exactly once delivery. This is the highest quality of service, for use when neither loss nor duplication of messages are acceptable.
     * There is an increased overhead associated with this quality of service.
     */
    @ApiModelProperty(value = "Exactly once delivery")
    EXACTLY_ONCE(2, "Exactly once");

    @ApiModelProperty(value = "Numeric value of the QoS level")
    private Integer value;

    @ApiModelProperty(value = "Description of the QoS level")
    private String desc;

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    /**
     * Get the enum constant that matches the given value.
     *
     * @param value The integer value of the QoS level.
     * @return An Optional containing the matched QoS enum or an empty Optional if no match is found.
     */
    public static Optional<QosEnum> fromValue(int value) {
        return Arrays.stream(QosEnum.values())
                .filter(qos -> qos.getValue() == value)
                .findFirst();
    }

    /**
     * Get description for a given value.
     *
     * @param value The integer value of the QoS level.
     * @return The description of the QoS level.
     */
    public static String getDescriptionByValue(int value) {
        return fromValue(value)
                .map(QosEnum::getDesc)
                .orElse("Unknown");
    }

    /**
     * Checks if the value is one of the defined enum constants.
     *
     * @param value The integer value to check against enum constants.
     * @return True if the value matches an enum constant, false otherwise.
     */
    public static boolean isValidType(int value) {
        return Arrays.stream(QosEnum.values())
                .anyMatch(qos -> qos.getValue() == value);
    }

    /**
     * Provides the enum constant as a string representation.
     *
     * @return The string representation of the enum constant.
     */
    @Override
    public String toString() {
        return String.format("QoS{value=%d, description='%s'}", value, desc);
    }
}
