package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.entity.dao;

import lombok.Data;

/**
 * @Description:
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2021/11/5$ 16:33$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/5$ 16:33$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class GB32960CustomData {

    private double energyConsumption;
    private int handbrakeStatus;
    private int highVoltagePower;

}