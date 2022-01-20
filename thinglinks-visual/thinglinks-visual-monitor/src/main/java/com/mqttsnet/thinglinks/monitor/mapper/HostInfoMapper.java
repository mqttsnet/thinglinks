package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.HostInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:HostInfoDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: ，暂未用
 */
public interface HostInfoMapper {

    List<HostInfo> selectByParams(Map<String, Object> params);

    HostInfo selectById(String id);

    void save(HostInfo HostInfo);

    int deleteById(String[] ids);

    int deleteByIp(String[] ips);

    int updateById(HostInfo HostInfo);
}
