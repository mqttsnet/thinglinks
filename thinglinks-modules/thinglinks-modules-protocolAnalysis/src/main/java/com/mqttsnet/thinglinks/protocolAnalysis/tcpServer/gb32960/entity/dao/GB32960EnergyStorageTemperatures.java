package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.entity.dao;
import lombok.Data;

import java.util.List;


@Data
public class GB32960EnergyStorageTemperatures {

    private Integer energyStorageSubSystemIndex1;
    private Integer probeCount;
    private List<Integer> cellTemperatures;

}