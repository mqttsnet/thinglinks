package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.AppState;
import com.mqttsnet.thinglinks.monitor.mapper.AppStateMapper;
import com.mqttsnet.thinglinks.monitor.service.AppStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:AppStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: AppStateService.java
 */
@Service
public class AppStateServiceImpl implements AppStateService {

    @Autowired
    AppStateMapper appStateMapper;

    @Override
    public List<AppState> selectByParams(Map<String, Object> params) {
        List<AppState> list = appStateMapper.selectByParams(params);
        return list;
    }

    @Override
    public void save(AppState AppState) {
        AppState.setId(UUID.getUUID());
        AppState.setCreateTime(DateUtils.getNowTime());
        AppState.setDateStr(DateUtils.getDateTimeString(AppState.getCreateTime()));
        appStateMapper.save(AppState);
    }

    @Override
    public void saveRecord(List<AppState> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        for (AppState as : recordList) {
            as.setId(UUID.getUUID());
            as.setDateStr(DateUtils.getDateTimeString(as.getCreateTime()));
        }
        appStateMapper.insertList(recordList);
    }

    @Override
    public int deleteByAppInfoId(String appInfoId) {
        return appStateMapper.deleteByAppInfoId(appInfoId);
    }

    @Override
    public int deleteById(String[] ids) {
        return appStateMapper.deleteById(ids);
    }

    @Override
    public AppState selectById(String id) {
        return appStateMapper.selectById(id);
    }
}
