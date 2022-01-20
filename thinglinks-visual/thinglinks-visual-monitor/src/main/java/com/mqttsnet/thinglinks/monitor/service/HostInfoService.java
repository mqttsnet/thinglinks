package com.mqttsnet.thinglinks.monitor.service;

import com.mqttsnet.thinglinks.monitor.api.domain.HostInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:HostInfoService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 暂未用
 */
public interface HostInfoService {

    List<HostInfo> selectByParams(Map<String, Object> params);

    void save(HostInfo HostInfo);

    int deleteById(String[] id);

    int deleteByIp(String[] ip);

    void updateById(HostInfo HostInfo);

    HostInfo selectById(String id);

}
