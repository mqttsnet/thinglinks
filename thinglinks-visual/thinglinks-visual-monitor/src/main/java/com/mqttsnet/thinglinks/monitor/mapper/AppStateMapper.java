package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.AppState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:AppStateDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: AppStateDao.java
 */
public interface AppStateMapper {

    List<AppState> selectByParams(Map<String, Object> params);

    AppState selectById(String id);

    void save(AppState AppState);

    void insertList(List<AppState> recordList);

    int deleteByAppInfoId(String appInfoId);

    int deleteByDate(Map<String, Object> map);

    int deleteById(String[] ids);
}
