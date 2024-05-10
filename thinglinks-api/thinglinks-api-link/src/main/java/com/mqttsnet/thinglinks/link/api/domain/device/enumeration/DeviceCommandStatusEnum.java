package com.mqttsnet.thinglinks.link.api.domain.device.enumeration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * -----------------------------------------------------------------------------
 * File Name: DeviceCommandStatusEnum.java
 * -----------------------------------------------------------------------------
 * Description:
 * Enumeration for Device Command Status
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
 * @date 2023-11-11 16:35
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DeviceCommandStatusEnum", description = "Enumeration for Device Command Status")
public enum DeviceCommandStatusEnum {

    /**
     * Pending status.
     */
    @ApiModelProperty(value = "Pending status")
    PENDING(0, "待处理"),

    /**
     * Success status.
     */
    @ApiModelProperty(value = "Success status")
    SUCCESS(1, "成功"),

    /**
     * Failure status.
     */
    @ApiModelProperty(value = "Failure status")
    FAILURE(2, "失败");

    @ApiModelProperty(value = "Command status value")
    private Integer value;

    @ApiModelProperty(value = "Command status description")
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
     * @param value The integer value of the command status.
     * @return An Optional containing the matched DeviceCommandStatusEnum or an empty Optional if no match is found.
     */
    public static Optional<DeviceCommandStatusEnum> fromValue(Integer value) {
        return Arrays.stream(DeviceCommandStatusEnum.values())
                .filter(status -> status.getValue().equals(value))
                .findFirst();
    }

    /**
     * Checks if the value is one of the defined enum constants.
     *
     * @param value The integer value to check against enum constants.
     * @return True if the value matches an enum constant, false otherwise.
     */
    public static boolean isValidStatus(Integer value) {
        return Arrays.stream(DeviceCommandStatusEnum.values())
                .anyMatch(status -> status.getValue().equals(value));
    }

    /**
     * Get description for a given value.
     *
     * @param value The integer value of the command status.
     * @return The description of the command status.
     */
    public static Optional<String> getDescriptionByValue(Integer value) {
        return fromValue(value).map(DeviceCommandStatusEnum::getDesc);
    }

    /**
     * Provides the enum constant as a string representation.
     *
     * @return The string representation of the enum constant.
     */
    @Override
    public String toString() {
        return String.format("Status: %d, Description: %s", value, desc);
    }
}
