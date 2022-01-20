package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.LogInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:LogInfoService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: LogInfoService.java
 */
public interface LogInfoService {

    List<LogInfo> selectByParams(Map<String, Object> params);

    void saveRecord(List<LogInfo> recordList);

    void save(String hostname, String infoContent, String state);

    int countByParams(Map<String, Object> params);

    int deleteById(String[] ids);

    LogInfo selectById(String id);

}
