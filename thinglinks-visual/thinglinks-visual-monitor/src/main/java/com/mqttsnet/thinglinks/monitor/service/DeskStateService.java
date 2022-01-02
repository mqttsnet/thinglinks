package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.DeskState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:DeskStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: DeskStateService.java
 */
public interface DeskStateService {

    List<DeskState> selectByParams(Map<String, Object> params);

    void save(DeskState DeskState);

    void saveRecord(List<DeskState> recordList);

    int deleteById(String[] ids);

    DeskState selectById(String id);

    int deleteByAccHname(Map<String, Object> params);

}
