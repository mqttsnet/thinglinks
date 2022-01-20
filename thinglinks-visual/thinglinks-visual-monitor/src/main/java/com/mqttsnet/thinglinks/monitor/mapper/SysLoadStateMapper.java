package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.SysLoadState;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:SysLoadStateDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 查看uptime查看系统负载状态
 */
public interface SysLoadStateMapper {

    List<SysLoadState> selectByParams(Map<String, Object> params);

    SysLoadState selectById(String id);

    void save(SysLoadState SysLoadState);

    void insertList(List<SysLoadState> recordList);

    int deleteByAccountAndDate(Map<String, Object> map);

    int deleteById(String[] id);
}
