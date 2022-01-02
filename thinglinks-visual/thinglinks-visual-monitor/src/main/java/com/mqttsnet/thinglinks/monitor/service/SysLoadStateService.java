package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.SysLoadState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:SysLoadStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: SysLoadStateService.java
 */
public interface SysLoadStateService {

    List<SysLoadState> selectByParams(Map<String, Object> params);

    void save(SysLoadState SysLoadState);

    void saveRecord(List<SysLoadState> recordList);

    int deleteById(String[] ids);

    SysLoadState selectById(String id);

}
