package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.LogInfo;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:LogInfoDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 查看日志信息
 */
public interface LogInfoMapper {

    int countByParams(Map<String, Object> params);

    List<LogInfo> selectByParams(Map<String, Object> params);

    LogInfo selectById(String id);

    void save(LogInfo LogInfo);

    int deleteById(String[] ids);

    void insertList(List<LogInfo> recordList);

    int deleteByDate(Map<String, Object> map);

}
