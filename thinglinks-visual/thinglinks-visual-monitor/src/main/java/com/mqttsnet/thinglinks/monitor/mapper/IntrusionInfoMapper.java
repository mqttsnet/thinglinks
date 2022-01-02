package com.mqttsnet.thinglinks.monitor.mapper;

import com.mqttsnet.thinglinks.monitor.api.domain.IntrusionInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:IntrusionInfoDao.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 查看系统入侵信息
 */
public interface IntrusionInfoMapper {

    List<IntrusionInfo> selectByAccountId(String accountId);

    List<IntrusionInfo> selectByParams(Map<String, Object> params);

    IntrusionInfo selectById(String id);

    void save(IntrusionInfo IntrusionInfo);

    void insertList(List<IntrusionInfo> recordList);

    int deleteById(String[] ids);

    int deleteByAccountAndDate(Map<String, Object> map);

    int deleteByAccHname(Map<String, Object> map);


}
