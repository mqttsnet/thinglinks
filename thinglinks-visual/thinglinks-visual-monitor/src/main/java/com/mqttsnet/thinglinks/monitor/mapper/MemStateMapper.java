package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.MemState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:MemStateDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 查看内存使用情况
 */
public interface MemStateMapper {

    List<MemState> selectByParams(Map<String, Object> params);

    MemState selectById(String id);

    void save(MemState MemState);

    void insertList(List<MemState> recordList);

    int deleteByAccountAndDate(Map<String, Object> map);

    int deleteById(String[] ids);
}
