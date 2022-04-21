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
public class GB32960VehicleStatus {

    /**
     * 车辆状态
     */
    private String engineStatus;
    /**
     * 运行模式
     */
    private String runningModel;
    /**
     * 充电状态
     */
    private String chargingStatus;
    /**
     * 车速
     */
    private Double speed;
    /**
     * 累计里程
     */
    private Double mileage;
    /**
     * 总电压
     */
    private Double voltage;
    /**
     * 总电流
     */
    private Double current;
    /**
     * SOC
     */
    private Integer soc;
    /**
     * DC-DC状态
     */
    private String dcStatus;
    /**
     * 档位信息
     */
    private GB32960TransmissionStatus transmissionStatus;
    private Integer insulationResistance;
    private Integer accelerationPedalTravel;
    private Integer brakePedalState;

}