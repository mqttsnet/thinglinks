package com.mqttsnet.thinglinks.link.service.protocol.impl;

import com.mqttsnet.thinglinks.link.api.domain.protocol.Protocol;
import com.mqttsnet.thinglinks.link.mapper.protocol.ProtocolMapper;
import com.mqttsnet.thinglinks.link.service.protocol.ProtocolService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
/**
* @program: thinglinks
* @description: ${description}
* @packagename: com.mqttsnet.thinglinks.link.service.protocol.impl
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-07-01 17:56
**/
@Service
public class ProtocolServiceImpl implements ProtocolService{

    @Resource
    private ProtocolMapper protocolMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return protocolMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Protocol record) {
        return protocolMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(Protocol record) {
        return protocolMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(Protocol record) {
        return protocolMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(Protocol record) {
        return protocolMapper.insertSelective(record);
    }

    @Override
    public Protocol selectByPrimaryKey(Long id) {
        return protocolMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Protocol record) {
        return protocolMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Protocol record) {
        return protocolMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<Protocol> list) {
        return protocolMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<Protocol> list) {
        return protocolMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<Protocol> list) {
        return protocolMapper.batchInsert(list);
    }

}
