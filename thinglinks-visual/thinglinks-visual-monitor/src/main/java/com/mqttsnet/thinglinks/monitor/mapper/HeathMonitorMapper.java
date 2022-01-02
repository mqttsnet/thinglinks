package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.HeathMonitor;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:HeathMonitorMapper.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: HeathMonitorDao.java
 */
public interface HeathMonitorMapper {

    List<HeathMonitor> selectByParams(Map<String, Object> params);

    HeathMonitor selectById(String id);

    void save(HeathMonitor HeathMonitor);

    void insertList(List<HeathMonitor> recordList);

    int deleteById(String[] ids);

    int deleteByDate(Map<String, Object> map);

    int countByParams(Map<String, Object> params);

    void updateList(List<HeathMonitor> recordList);

    void updateById(HeathMonitor HeathMonitor);
}
