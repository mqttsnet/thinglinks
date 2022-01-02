package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.NetIoState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:NetIoStateDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 查看网络设备的吞吐率
 */
public interface NetIoStateMapper {

    List<NetIoState> selectByParams(Map<String, Object> params);

    NetIoState selectById(String id);

    void save(NetIoState NetIoState);

    void insertList(List<NetIoState> recordList);

    int deleteByAccountAndDate(Map<String, Object> map);

    int deleteById(String[] ids);

}
