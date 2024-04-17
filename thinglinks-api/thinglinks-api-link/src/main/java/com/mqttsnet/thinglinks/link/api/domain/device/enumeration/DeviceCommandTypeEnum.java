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
 * File Name: DeviceCommandTypeEnum.java
 * -----------------------------------------------------------------------------
 * Description:
 * Enumeration for Device Command Types
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
 * @date 2023-11-11 16:27
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DeviceCommandTypeEnum", description = "Enumeration for Device Command Types")
public enum DeviceCommandTypeEnum {

    /**
     * Command issue.
     */
    @ApiModelProperty(value = "Command issue")
    COMMAND_ISSUE(0, "命名下发"),

    /**
     * Command response.
     */
    @ApiModelProperty(value = "Command response")
    COMMAND_RESPONSE(1, "命令响应");

    @ApiModelProperty(value = "Command type value")
    private Integer value;

    @ApiModelProperty(value = "Command type description")
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
     * @param value The integer value of the command type.
     * @return An Optional containing the matched CommandTypeEnum or an empty Optional if no match is found.
     */
    public static Optional<DeviceCommandTypeEnum> fromValue(Integer value) {
        return Arrays.stream(DeviceCommandTypeEnum.values())
                .filter(type -> type.getValue().equals(value))
                .findFirst();
    }

    /**
     * Checks if the value is one of the defined enum constants.
     *
     * @param value The integer value to check against enum constants.
     * @return True if the value matches an enum constant, false otherwise.
     */
    public static boolean isValidType(Integer value) {
        return Arrays.stream(DeviceCommandTypeEnum.values())
                .anyMatch(type -> type.getValue().equals(value));
    }

    /**
     * Get description for a given value.
     *
     * @param value The integer value of the command type.
     * @return The description of the command type.
     */
    public static Optional<String> getDescriptionByValue(Integer value) {
        return fromValue(value).map(DeviceCommandTypeEnum::getDesc);
    }

    /**
     * Provides the enum constant as a string representation.
     *
     * @return The string representation of the enum constant.
     */
    @Override
    public String toString() {
        return String.format("Type: %d, Description: %s", value, desc);
    }
}
