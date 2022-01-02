package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.SystemInfo;
import com.mqttsnet.thinglinks.monitor.mapper.SystemInfoMapper;
import com.mqttsnet.thinglinks.monitor.service.SystemInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SystemInfoServiceImpl implements SystemInfoService {

    @Autowired
    private SystemInfoMapper systemInfoMapper;

    @Override
    public List<SystemInfo> selectSystemInfoList(SystemInfo systemInfo) {
        return systemInfoMapper.selectSystemInfoList(systemInfo);
    }

    @Override
    public void save(SystemInfo systemInfo) {
        systemInfo.setId(UUID.getUUID());
        systemInfo.setCreateTime(DateUtils.getNowTime());
        systemInfoMapper.save(systemInfo);
    }

    @Override
    public void saveRecord(List<SystemInfo> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        for (SystemInfo as : recordList) {
            as.setId(UUID.getUUID());
            as.setCreateTime(DateUtils.getNowTime());
        }
        systemInfoMapper.insertList(recordList);
    }

    @Override
    public void updateRecord(List<SystemInfo> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        systemInfoMapper.updateList(recordList);
    }

    @Override
    public void updateById(SystemInfo systemInfo) {
        systemInfoMapper.updateById(systemInfo);
    }

    @Override
    public int deleteById(String[] ids) {
        return systemInfoMapper.deleteById(ids);
    }

    @Override
    public SystemInfo selectById(String id) {
        return systemInfoMapper.selectById(id);
    }

    @Override
    public int countByParams(Map<String, Object> params) {
        return systemInfoMapper.countByParams(params);
    }

    @Override
    public List<SystemInfo> selectByAccountId(String accountId) {
        return systemInfoMapper.selectByAccountId(accountId);
    }

    @Override
    public int deleteByAccHname(Map<String, Object> params) {
        return systemInfoMapper.deleteByAccHname(params);
    }
}
