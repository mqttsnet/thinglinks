package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.NetIoState;
import com.mqttsnet.thinglinks.monitor.mapper.NetIoStateMapper;
import com.mqttsnet.thinglinks.monitor.service.NetIoStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:NetIoStateService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: NetIoStateService.java
 */
@Service
public class NetIoStateServiceImpl implements NetIoStateService {

    @Autowired
    private NetIoStateMapper netIoStateMapper;

    @Override
    public List<NetIoState> selectByParams(Map<String, Object> params) {
        List<NetIoState> list = netIoStateMapper.selectByParams(params);
        return list;
    }

    @Override
    public void save(NetIoState NetIoState) {
        NetIoState.setId(UUID.getUUID());
        NetIoState.setCreateTime(DateUtils.getNowTime());
        NetIoState.setDateStr(DateUtils.getDateTimeString(NetIoState.getCreateTime()));
        netIoStateMapper.save(NetIoState);
    }

    @Override
    public void saveRecord(List<NetIoState> recordList) {
        if (recordList.size() < 1) {
            return;
        }
        for (NetIoState as : recordList) {
            as.setId(UUID.getUUID());
            as.setDateStr(DateUtils.getDateTimeString(as.getCreateTime()));
        }
        netIoStateMapper.insertList(recordList);
    }

    @Override
    public int deleteById(String[] id) {
        return netIoStateMapper.deleteById(id);
    }

    @Override
    public NetIoState selectById(String ids) {
        return netIoStateMapper.selectById(ids);
    }

}
