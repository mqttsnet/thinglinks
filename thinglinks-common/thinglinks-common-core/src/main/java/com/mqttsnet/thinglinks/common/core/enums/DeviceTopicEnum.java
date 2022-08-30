package com.mqttsnet.thinglinks.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @program: thinglinks
 * @description: 设备Topic枚举
 * @packagename: com.mqttsnet.thinglinks.common.core.enums
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-08-26 10:49
 **/
@Getter
@AllArgsConstructor
public enum DeviceTopicEnum {
    /**
     * 基础Topic
     */
    BASIS("0","基础Topic"),

    /**
     * 自定义Topic
     */
    CUSTOM("1","自定义Topic");

    private  String key;
    private  String value;
}
