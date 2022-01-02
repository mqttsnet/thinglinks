package com.mqttsnet.thinglinks.monitor.service.impl;
import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.AppInfo;
import com.mqttsnet.thinglinks.monitor.mapper.AppInfoMapper;
import com.mqttsnet.thinglinks.monitor.mapper.AppStateMapper;
import com.mqttsnet.thinglinks.monitor.service.AppInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@Service
public class AppInfoServiceImpl implements AppInfoService {

    @Autowired
    private AppInfoMapper appInfoMapper;

    @Autowired
    private AppStateMapper appStateMapper;

    @Override
    public List<AppInfo> selectByParams(Map<String, Object> params) {
        return appInfoMapper.selectByParams(params);
    }

    @Override
    public void save(AppInfo AppInfo) {
        AppInfo.setId(UUID.getUUID());
        AppInfo.setCreateTime(DateUtils.getNowTime());
        if (!StringUtils.isEmpty(AppInfo.getAppPid())) {
            AppInfo.setAppPid(AppInfo.getAppPid().trim());
        }
        appInfoMapper.save(AppInfo);
    }

    @Override
    public int deleteByHostName(Map<String, Object> map) {
        return appInfoMapper.deleteByHostName(map);
    }

    @Override
    public void saveRecord(List<AppInfo> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        for (AppInfo as : recordList) {
            as.setId(UUID.getUUID());
        }
        appInfoMapper.insertList(recordList);
    }

    @Override
    public int countByParams(Map<String, Object> params) {
        return appInfoMapper.countByParams(params);
    }

    @Override
    public int deleteById(String[] ids) {
        for (String appInfoId : ids) {
            appStateMapper.deleteByAppInfoId(appInfoId);
        }
        return appInfoMapper.deleteById(ids);
    }

    @Override
    public void updateRecord(List<AppInfo> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        appInfoMapper.updateList(recordList);
    }

    @Override
    public void updateById(AppInfo AppInfo) {
        appInfoMapper.updateById(AppInfo);
    }

    @Override
    public AppInfo selectById(String id) {
        return appInfoMapper.selectById(id);
    }

}
