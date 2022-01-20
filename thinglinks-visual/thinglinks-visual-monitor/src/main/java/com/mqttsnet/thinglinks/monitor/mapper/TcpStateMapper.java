package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.TcpState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:TcpStateDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 查看TCP连接状态
 */
public interface TcpStateMapper {

    List<TcpState> selectByParams(Map<String, Object> params);

    TcpState selectById(String id);

    void save(TcpState TcpState);

    void insertList(List<TcpState> recordList);

    int deleteByAccountAndDate(Map<String, Object> map);

    int deleteById(String[] id);

}
