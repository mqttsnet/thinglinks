package com.mqttsnet.thinglinks.link.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 连接状态
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2021/10/25$ 15:54$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/10/25$ 15:54$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum DeviceConnectStatus {
    /**
     * 初始化
     */
    INIT("INIT","INIT"),

    /**
     * 在线
     */
    ONLINE("ONLINE","ONLINE"),

    /**
     * 离线
     */
    OFFLINE("OFFLINE","OFFLINE");

    private  String key;
    private  String value;
}
