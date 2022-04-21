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
public class GB32960DriveMotors {

    /**
     * 驱动电机序号
     */
    private Integer sn;
    /**
     * 驱动电机状态
     */
    private String driveMotorPower;
    /**
     * 驱动电机控制器温度
     */
    private Integer controllerTemperature;
    /**
     * 驱动电机转速
     */
    private Integer driveMotorSpeed;
    /**
     * 驱动电机转矩
     */
    private Double driveMotorTorque;
    /**
     * 驱动电机温度
     */
    private Integer driveMotorTemperature;
    /**
     * 电机控制器输入电压
     */
    private Double controllerInputVoltage;
    /**
     * 电机控制器直流母线电流
     */
    private Double dcBusCurrentOfController;

}