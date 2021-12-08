package com.mqttsnet.thinglinks.link.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 设备类型
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2021/10/25$ 16:19$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/10/25$ 16:19$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum DeviceType {

    /**
     * 普通设备（无子设备也无父设备）
     */
    COMMON("COMMON","COMMON"),

    /**
     * 网关设备(可挂载子设备)
     */
    GATEWAY("GATEWAY","GATEWAY"),

    /**
     * 子设备(归属于某个网关设备)
     */
    SUBSET("SUBSET","SUBSET");

    private  String key;
    private  String value;
}
