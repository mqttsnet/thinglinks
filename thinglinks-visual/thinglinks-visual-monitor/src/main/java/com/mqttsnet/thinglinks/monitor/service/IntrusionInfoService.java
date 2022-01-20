package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.IntrusionInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:IntrusionInfoService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: IntrusionInfoService.java
 */
public interface IntrusionInfoService {

    List<IntrusionInfo> selectByParams(Map<String, Object> params);

    void save(IntrusionInfo IntrusionInfo);

    void saveRecord(List<IntrusionInfo> recordList);

    int deleteById(String[] ids);

    IntrusionInfo selectById(String id);

    List<IntrusionInfo> selectByAccountId(String accountId);

}
