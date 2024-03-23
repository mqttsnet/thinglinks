package com.mqttsnet.thinglinks.link.api.domain.device.enumeration;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * @program: thinglinks
 * @description: MQTT协议Topo 状态枚举
 * @packagename: com.mqttsnet.thinglinks.link.api.domain.device.enumeration
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-20 17:51
 **/
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "MqttProtocolTopoStatusEnum", description = "MQTT协议Topo 状态枚举")
public enum MqttProtocolTopoStatusEnum {
    /**
     * 成功
     */
    SUCCESS(0, "success"),

    /**
     * 失败
     */
    FAILURE(1, "failure"),
    ;

    private Integer value;
    private String desc;

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
