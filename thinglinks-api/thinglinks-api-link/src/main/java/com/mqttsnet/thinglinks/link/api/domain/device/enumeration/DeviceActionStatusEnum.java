package com.mqttsnet.thinglinks.link.api.domain.device.enumeration;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 设备动作状态 枚举
 * </p>
 *
 * @author shihuan sun
 * @date 2023-08-20
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "DeviceActionStatusEnum", description = "设备动作状态 枚举")
public enum DeviceActionStatusEnum {
    /**
     * 成功
     */
    SUCCESSFUL("successful", "成功"),

    /**
     * 失败
     */
    FAIL("fall", "失败"),
    ;

    private String value;
    private String desc;

    public void setValue(String value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    /**
     * 根据key获取对应的枚举
     *
     * @param value 设备连接的状态值
     * @return 返回对应的枚举，如果没找到则返回 Optional.empty()
     */
    public static Optional<DeviceActionStatusEnum> fromValue(Integer value) {
        return Stream.of(DeviceActionStatusEnum.values())
                .filter(status -> status.getValue().equals(value))
                .findFirst();
    }


}
