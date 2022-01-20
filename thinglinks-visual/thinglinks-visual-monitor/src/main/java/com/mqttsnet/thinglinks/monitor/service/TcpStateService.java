package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.TcpState;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:TcpStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: TcpStateService.java
 */
public interface TcpStateService {

    List<TcpState> selectByParams(Map<String, Object> params);

    public void save(TcpState TcpState);

    public void saveRecord(List<TcpState> recordList);

    public int deleteById(String[] ids);

    public TcpState selectById(String id);

}
