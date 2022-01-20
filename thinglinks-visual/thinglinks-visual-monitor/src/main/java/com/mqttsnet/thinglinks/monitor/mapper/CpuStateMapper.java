package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.CpuState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:CpuStateDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: CpuStateDao.java
 */
public interface CpuStateMapper {

    List<CpuState> selectByParams(Map<String, Object> params);

    CpuState selectById(String id);

    void save(CpuState CpuState);

    void insertList(List<CpuState> recordList);

    int deleteByAccountAndDate(Map<String, Object> map);

    int deleteById(String[] ids);
}
