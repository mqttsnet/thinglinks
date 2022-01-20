package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.IntrusionInfo;
import com.mqttsnet.thinglinks.monitor.mapper.IntrusionInfoMapper;
import com.mqttsnet.thinglinks.monitor.service.IntrusionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:IntrusionInfoService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: IntrusionInfoService.java
 */
@Service
public class IntrusionInfoServiceImpl implements IntrusionInfoService {

    @Autowired
    private IntrusionInfoMapper intrusionInfoMapper;

    @Override
    public List<IntrusionInfo> selectByParams(Map<String, Object> params) {
        List<IntrusionInfo> list = intrusionInfoMapper.selectByParams(params);
        return list;
    }

    @Override
    public void save(IntrusionInfo IntrusionInfo) {
        IntrusionInfo.setId(UUID.getUUID());
        IntrusionInfo.setCreateTime(DateUtils.getNowTime());
        intrusionInfoMapper.save(IntrusionInfo);
    }

    @Override
    public void saveRecord(List<IntrusionInfo> recordList) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (IntrusionInfo as : recordList) {
            as.setId(UUID.getUUID());
            map.put("hostname", as.getHostname());
            intrusionInfoMapper.deleteByAccHname(map);
        }
        intrusionInfoMapper.insertList(recordList);
    }

    @Override
    public int deleteById(String[] ids) {
        return intrusionInfoMapper.deleteById(ids);
    }

    @Override
    public IntrusionInfo selectById(String id) {
        return intrusionInfoMapper.selectById(id);
    }

    @Override
    public List<IntrusionInfo> selectByAccountId(String accountId) {
        return intrusionInfoMapper.selectByAccountId(accountId);
    }


}
