package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.AppInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:AppInfoDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: AppInfoDao.java
 */
public interface AppInfoMapper {

    List<AppInfo> selectByParams(Map<String, Object> params);

    AppInfo selectById(String id);

    List<AppInfo> selectByAccountId(String accountId);

    void save(AppInfo AppInfo);

    void insertList(List<AppInfo> recordList);

    void updateList(List<AppInfo> recordList);

    int deleteById(String[] id);

    int deleteByHostName(Map<String, Object> map);

    int deleteByDate(Map<String, Object> map);

    int countByParams(Map<String, Object> params);

    int updateById(AppInfo AppInfo);
}
