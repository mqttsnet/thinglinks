package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.SysLoadState;
import com.mqttsnet.thinglinks.monitor.mapper.SysLoadStateMapper;
import com.mqttsnet.thinglinks.monitor.service.SysLoadStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:SysLoadStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: SysLoadStateService.java
 */
@Service
public class SysLoadStateServiceImpl implements SysLoadStateService {

    @Autowired
    private SysLoadStateMapper sysLoadStateMapper;

    @Override
    public List<SysLoadState> selectByParams(Map<String, Object> params) {
        List<SysLoadState> list = sysLoadStateMapper.selectByParams(params);
        return list;
    }

    @Override
    public void save(SysLoadState SysLoadState) {
        SysLoadState.setId(UUID.getUUID());
        SysLoadState.setCreateTime(DateUtils.getNowTime());
        SysLoadState.setDateStr(DateUtils.getDateTimeString(SysLoadState.getCreateTime()));
        sysLoadStateMapper.save(SysLoadState);
    }

    @Override
    public void saveRecord(List<SysLoadState> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        for (SysLoadState as : recordList) {
            as.setId(UUID.getUUID());
            as.setDateStr(DateUtils.getDateTimeString(as.getCreateTime()));
        }
        sysLoadStateMapper.insertList(recordList);
    }

    @Override
    public int deleteById(String[] ids) {
        return sysLoadStateMapper.deleteById(ids);
    }

    @Override
    public SysLoadState selectById(String id) {
        return sysLoadStateMapper.selectById(id);
    }

}
