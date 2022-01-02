package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.DeskState;
import com.mqttsnet.thinglinks.monitor.mapper.DeskStateMapper;
import com.mqttsnet.thinglinks.monitor.service.DeskStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:DeskStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: DeskStateService.java
 */
@Service
public class DeskStateServiceImpl implements DeskStateService {

    @Autowired
    private DeskStateMapper deskStateMapper;

    @Override
    public List<DeskState> selectByParams(Map<String, Object> params) {
        List<DeskState> list = deskStateMapper.selectByParams(params);
        return list;
    }

    @Override
    public void save(DeskState DeskState) {
        DeskState.setId(UUID.getUUID());
        DeskState.setCreateTime(DateUtils.getNowTime());
        DeskState.setDateStr(DateUtils.getDateTimeString(DeskState.getCreateTime()));
        deskStateMapper.save(DeskState);
    }

    @Transactional
    @Override
    public void saveRecord(List<DeskState> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        for (DeskState as : recordList) {
            as.setId(UUID.getUUID());
            as.setDateStr(DateUtils.getDateTimeString(as.getCreateTime()));
        }
        deskStateMapper.insertList(recordList);
    }

    @Override
    public int deleteById(String[] ids) {
        return deskStateMapper.deleteById(ids);
    }

    @Override
    public DeskState selectById(String id) {
        return deskStateMapper.selectById(id);
    }

    @Override
    public int deleteByAccHname(Map<String, Object> params) {
        return deskStateMapper.deleteByAccHname(params);
    }

}
