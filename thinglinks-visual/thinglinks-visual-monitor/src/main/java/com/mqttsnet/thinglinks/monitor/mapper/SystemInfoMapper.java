package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.SystemInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:SystemInfoDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 查看系统信息
 */
public interface SystemInfoMapper {

    List<SystemInfo> selectSystemInfoList(SystemInfo systemInfo);

    List<SystemInfo> selectByAccountId(String accountId);

    void insertList(List<SystemInfo> recordList);

    void updateList(List<SystemInfo> recordList);

    SystemInfo selectById(String id);

    int updateById(SystemInfo SystemInfo);

    int countByParams(Map<String, Object> params);

    void save(SystemInfo SystemInfo);

    int deleteById(String[] ids);

    int deleteByAccountAndDate(Map<String, Object> map);

    int deleteByAccHname(Map<String, Object> map);

}
