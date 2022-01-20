package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.TcpState;
import com.mqttsnet.thinglinks.monitor.mapper.TcpStateMapper;
import com.mqttsnet.thinglinks.monitor.service.TcpStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TcpStateServiceImpl implements TcpStateService {

    @Autowired
    private TcpStateMapper tcpStateMapper;

    @Override
    public List<TcpState> selectByParams(Map<String, Object> params) {
        return tcpStateMapper.selectByParams(params);
    }

    @Override
    public void save(TcpState TcpState) {
        TcpState.setId(UUID.getUUID());
        TcpState.setCreateTime(DateUtils.getNowTime());
        TcpState.setDateStr(DateUtils.getDateTimeString(TcpState.getCreateTime()));
        tcpStateMapper.save(TcpState);
    }

    @Override
    public void saveRecord(List<TcpState> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        for (TcpState as : recordList) {
            as.setId(UUID.getUUID());
            as.setDateStr(DateUtils.getDateTimeString(as.getCreateTime()));
        }
        tcpStateMapper.insertList(recordList);
    }

    @Override
    public int deleteById(String[] ids) {
        return tcpStateMapper.deleteById(ids);
    }

    @Override
    public TcpState selectById(String id) {
        return tcpStateMapper.selectById(id);
    }

}
