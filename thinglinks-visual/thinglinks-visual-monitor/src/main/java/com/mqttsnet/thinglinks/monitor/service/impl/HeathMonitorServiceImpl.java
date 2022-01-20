package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.HeathMonitor;
import com.mqttsnet.thinglinks.monitor.mapper.HeathMonitorMapper;
import com.mqttsnet.thinglinks.monitor.service.HeathMonitorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:HeathMonitorService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: HeathMonitorService.java
 */
@Service
public class HeathMonitorServiceImpl implements HeathMonitorService {

    @Autowired
    private HeathMonitorMapper heathMonitorMapper;

    @Override
    public List<HeathMonitor> selectByParams(Map<String, Object> params) {
        List<HeathMonitor> list = heathMonitorMapper.selectByParams(params);
        return list;
    }

    @Override
    public void save(HeathMonitor HeathMonitor) {
        HeathMonitor.setId(UUID.getUUID());
        HeathMonitor.setCreateTime(DateUtils.getNowTime());
        if (StringUtils.isEmpty(HeathMonitor.getHeathUrl())) {
            HeathMonitor.setHeathUrl(HeathMonitor.getHeathUrl().trim());
        }
        heathMonitorMapper.save(HeathMonitor);
    }


    @Transactional
    @Override
    public void saveRecord(List<HeathMonitor> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        for (HeathMonitor as : recordList) {
            as.setId(UUID.getUUID());
        }
        heathMonitorMapper.insertList(recordList);
    }

    @Override
    public int countByParams(Map<String, Object> params) {
        return heathMonitorMapper.countByParams(params);
    }

    @Override
    @Transactional
    public int deleteById(String[] id) {
        return heathMonitorMapper.deleteById(id);
    }

    @Override
    public void updateById(HeathMonitor HeathMonitor) {
        if (StringUtils.isEmpty(HeathMonitor.getHeathUrl())) {
            HeathMonitor.setHeathUrl(HeathMonitor.getHeathUrl().trim());
        }
        heathMonitorMapper.updateById(HeathMonitor);
    }

    @Override
    public HeathMonitor selectById(String id) {
        return heathMonitorMapper.selectById(id);
    }

    @Override
    @Transactional
    public void updateRecord(List<HeathMonitor> recordList) {
        heathMonitorMapper.updateList(recordList);
    }

}
