package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.NetIoState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:NetIoStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: NetIoStateService.java
 */
public interface NetIoStateService {

    List<NetIoState> selectByParams(Map<String, Object> params);

    void save(NetIoState NetIoState);

    void saveRecord(List<NetIoState> recordList);

    int deleteById(String[] ids);

    NetIoState selectById(String id);

}
