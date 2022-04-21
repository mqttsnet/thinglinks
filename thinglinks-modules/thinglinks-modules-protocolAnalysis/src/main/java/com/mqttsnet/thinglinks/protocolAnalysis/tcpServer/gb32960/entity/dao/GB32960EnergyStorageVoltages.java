package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.entity.dao;
import lombok.Data;

import java.util.List;

@Data
public class GB32960EnergyStorageVoltages {

    private Integer energyStorageSubSystemIndex;
    private Double energyStorageVoltage;
    private Double energyStorageCurrent;
    private Integer cellCount;
    private Integer frameCellStartIndex;
    private Integer frameCellCount;
    private List<Double> cellVoltages;

}