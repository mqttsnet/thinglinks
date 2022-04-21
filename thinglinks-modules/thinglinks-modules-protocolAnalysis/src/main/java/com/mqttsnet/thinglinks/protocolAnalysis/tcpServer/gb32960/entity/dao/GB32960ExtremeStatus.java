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
public class GB32960ExtremeStatus {

    /**
     * 最高电压电池子系统号
     */
    private Integer subSystemIndexOfMaxVoltage;
    /**
     * 最高电压电池单体代号
     */
    private Integer cellIndexOfMaxVoltage;
    /**
     * 电池单体电压最高值
     */
    private Double cellMaxVoltage;
    /**
     * 最低电压电池子系统号
     */
    private Integer subSystemIndexOfMinVoltage;
    /**
     *最低电压电池单体代号
     */
    private Integer cellIndexOfMinVoltage;
    /**
     * 电池单体电压最低值
     */
    private Double cellMinVoltage;
    /**
     * 最高温度子系统号
     */
    private Integer subSystemIndexOfMaxTemperature;
    /**
     *最高温度探针序号
     */
    private Integer probeIndexOfMaxTemperature;
    /**
     * 最高温度值
     */
    private Integer maxTemperature;
    /**
     * 最低温度子系统号
     */
    private Integer subSystemIndexOfMinTemperature;
    /**
     * 最低温度探针序号
     */
    private Integer probeIndexOfMinTemperature;
    /**
     * 最低温度值
     */
    private Integer minTemperature;

}