package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.HostInfo;
import com.mqttsnet.thinglinks.monitor.mapper.HostInfoMapper;
import com.mqttsnet.thinglinks.monitor.service.HostInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @ClassName:HostInfoService.java
 * @author: shisen
 * @date: 2021年12月29日
 * @Description: 暂未用
 */
@Service
public class HostInfoServiceImpl implements HostInfoService {

    @Autowired
    private HostInfoMapper hostInfoMapper;

    @Override
    public List<HostInfo> selectByParams(Map<String, Object> params) {
        List<HostInfo> list = hostInfoMapper.selectByParams(params);
        return list;
    }

    @Override
    @Transactional
    public void save(HostInfo HostInfo) {
        HostInfo.setId(UUID.getUUID());
        HostInfo.setCreateTime(DateUtils.getNowTime());
        hostInfoMapper.save(HostInfo);
    }

    @Transactional
    @Override
    public int deleteById(String[] id) {
        return hostInfoMapper.deleteById(id);
    }

    @Transactional
    @Override
    public int deleteByIp(String[] ip) {
        return hostInfoMapper.deleteByIp(ip);
    }

    @Override
    public void updateById(HostInfo HostInfo) {
        hostInfoMapper.updateById(HostInfo);
    }

    @Override
    public HostInfo selectById(String id) {
        return hostInfoMapper.selectById(id);
    }

}
