package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.HeathMonitor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:HeathMonitorService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: HeathMonitorService.java
 */
public interface HeathMonitorService {

    List<HeathMonitor> selectByParams(Map<String, Object> params);

    void save(HeathMonitor HeathMonitor);

    void saveRecord(List<HeathMonitor> recordList);

    int countByParams(Map<String, Object> params);

    int deleteById(String[] ids);

    void updateById(HeathMonitor HeathMonitor);

    HeathMonitor selectById(String id);

    void updateRecord(List<HeathMonitor> recordList);

}
