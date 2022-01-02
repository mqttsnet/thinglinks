package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.AppState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:AppStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: AppStateService.java
 */
public interface AppStateService {

    List<AppState> selectByParams(Map<String, Object> params);

    void save(AppState AppState);

    void saveRecord(List<AppState> recordList);

    int deleteByAppInfoId(String appInfoId);

    public int deleteById(String[] ids);

    AppState selectById(String id);

}
