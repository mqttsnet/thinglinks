package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.CpuState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:CpuStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: CpuStateService.java
 */
public interface CpuStateService {

    List<CpuState> selectByParams(Map<String, Object> params);

    void save(CpuState CpuState);

    void saveRecord(List<CpuState> recordList);

    int deleteById(String[] ids);

    CpuState selectById(String id);

}
