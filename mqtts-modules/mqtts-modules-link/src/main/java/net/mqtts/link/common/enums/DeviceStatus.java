package net.mqtts.link.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  设备状态
 * @author mqtts
 * @date 2021-10-22
 */
@Getter
@AllArgsConstructor
public enum DeviceStatus {

    /**
     * 启用
     */
    ENABLE("ENABLE","ENABLE"),

    /**
     * 禁用
     */
    DISABLE("DISABLE","DISABLE");

    private  String key;
    private  String value;

}
