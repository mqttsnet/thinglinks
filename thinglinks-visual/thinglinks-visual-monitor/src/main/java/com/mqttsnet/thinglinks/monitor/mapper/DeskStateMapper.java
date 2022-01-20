package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.DeskState;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:DeskStateDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 查看磁盘大小使用信息
 */
public interface DeskStateMapper {

    List<DeskState> selectByParams(Map<String, Object> params);

    DeskState selectById(String id);

    void save(DeskState DeskState);

    void insertList(List<DeskState> recordList);

    int deleteByAccountAndDate(Map<String, Object> map);

    int deleteById(String[] ids);

    int deleteByAccHname(Map<String, Object> map);

}
