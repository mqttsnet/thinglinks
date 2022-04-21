package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.entity.dao;
import lombok.Data;

import java.util.List;

/**
 * @author sunshihuan
 * 可充电储能装置温度数据
 */
@Data
public class GB32960EnergyStorageTemperatureStatus {

    private int subEnergyStorageSystemCount;

    private List<GB32960EnergyStorageTemperatures> energyStorageTemperatures;
}