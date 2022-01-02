package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.MemState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:MemStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: MemStateService.java
 */
public interface MemStateService {

    List<MemState> selectByParams(Map<String, Object> params);

    void save(MemState MemState);

    void saveRecord(List<MemState> recordList);

    int deleteById(String[] id);

    MemState selectById(String id);

}
