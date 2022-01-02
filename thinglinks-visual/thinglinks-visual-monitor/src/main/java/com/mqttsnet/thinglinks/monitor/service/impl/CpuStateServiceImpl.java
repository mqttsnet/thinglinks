package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.CpuState;
import com.mqttsnet.thinglinks.monitor.mapper.CpuStateMapper;
import com.mqttsnet.thinglinks.monitor.service.CpuStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:CpuStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: CpuStateService.java
 */
@Service
public class CpuStateServiceImpl implements CpuStateService {

    @Autowired
    private CpuStateMapper cpuStateMapper;

    @Override
    public List<CpuState> selectByParams(Map<String, Object> params) {
        List<CpuState> list = cpuStateMapper.selectByParams(params);
        return list;
    }

    @Override
    public void save(CpuState CpuState) {
        CpuState.setId(UUID.getUUID());
        CpuState.setCreateTime(DateUtils.getNowTime());
        CpuState.setDateStr(DateUtils.getDateTimeString(CpuState.getCreateTime()));
        cpuStateMapper.save(CpuState);
    }

    @Override
    public void saveRecord(List<CpuState> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        for (CpuState as : recordList) {
            as.setId(UUID.getUUID());
            as.setDateStr(DateUtils.getDateTimeString(as.getCreateTime()));
        }
        cpuStateMapper.insertList(recordList);
    }

    @Override
    public int deleteById(String[] ids) {
        return cpuStateMapper.deleteById(ids);
    }

    @Override
    public CpuState selectById(String id) {
        return cpuStateMapper.selectById(id);
    }

}
