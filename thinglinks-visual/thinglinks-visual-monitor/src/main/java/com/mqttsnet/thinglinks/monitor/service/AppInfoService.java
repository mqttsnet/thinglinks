package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.AppInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:AppInfoService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: AppInfoService.java
 */
public interface AppInfoService {

    List<AppInfo> selectByParams(Map<String, Object> params);

    void save(AppInfo AppInfo);

    int deleteByHostName(Map<String, Object> map);

    void saveRecord(List<AppInfo> recordList);

    int countByParams(Map<String, Object> params);

    int deleteById(String[] ids);

    void updateRecord(List<AppInfo> recordList);

    void updateById(AppInfo AppInfo);

    AppInfo selectById(String id);

}
