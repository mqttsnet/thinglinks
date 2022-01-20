package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.MemState;
import com.mqttsnet.thinglinks.monitor.mapper.MemStateMapper;
import com.mqttsnet.thinglinks.monitor.service.MemStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:MemStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: MemStateService.java
 */
@Service
public class MemStateServiceImpl implements MemStateService {

    @Autowired
    private MemStateMapper memStateMapper;

    @Override
    public List<MemState> selectByParams(Map<String, Object> params) {
        List<MemState> list = memStateMapper.selectByParams(params);
        return list;
    }

    @Override
    public void save(MemState MemState) {
        MemState.setId(UUID.getUUID());
        MemState.setCreateTime(DateUtils.getNowTime());
        MemState.setDateStr(DateUtils.getDateTimeString(MemState.getCreateTime()));
        memStateMapper.save(MemState);
    }

    @Override
    public void saveRecord(List<MemState> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        for (MemState as : recordList) {
            as.setId(UUID.getUUID());
            as.setDateStr(DateUtils.getDateTimeString(as.getCreateTime()));
        }
        memStateMapper.insertList(recordList);
    }

    @Override
    public int deleteById(String[] ids) {
        return memStateMapper.deleteById(ids);
    }

    @Override
    public MemState selectById(String id) {
        return memStateMapper.selectById(id);
    }

}
