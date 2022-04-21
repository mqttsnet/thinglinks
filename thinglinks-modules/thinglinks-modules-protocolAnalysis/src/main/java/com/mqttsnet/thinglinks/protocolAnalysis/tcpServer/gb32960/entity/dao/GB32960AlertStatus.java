package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.entity.dao;
import lombok.Data;

import java.util.List;

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
public class GB32960AlertStatus {

    /**
     * 最高报警等级,为当前发生的故障中的最高等级值，有效值范围：0～3
     */
    private String highestAlertLevel;
    /**
     * 可充电储能装置故障总数N1,N1个可充电储能装置故障，有效值范围：0～252，“0xFE”表示异常，“0xFF”表示无效
     */
    private int energyStorageAlertCount;
    /**
     * 可充电储能装置故障代码列表
     */
    private List<String> energyStorageAlertList;
    /**
     * 驱动电机,故障总数N2,N2个驱动电机故障，有效值范围：0～252，“0xFE”表示异常，“0xFF”表示无效
     */
    private int driveMotorAlertCount;
    /**
     * 驱动电机故障代码列表
     */
    private List<String> driveMotorAlertList;
    /**
     * 发动机故障总数N3,N3个驱动电机故障，有效值范围：0～252，“0xFE”表示异常，“0xFF”表示无效
     */
    private int engineAlertCount;
    /**
     *发动机故障列表
     */
    private List<String> engineAlertList;
    /**
     * 其他故障总数N4,N4个其他故障
     */
    private int otherAlertCount;
    /**
     * 其他故障代码列表
     */
    private List<String> otherAlertList;
    /**
     * 通用报警标志
     */
    private GB32960Alert alert;

}