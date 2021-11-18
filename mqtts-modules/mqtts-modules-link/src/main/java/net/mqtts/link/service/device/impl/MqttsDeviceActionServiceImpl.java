package net.mqtts.link.service.device.impl;

import net.mqtts.link.domain.device.MqttsDeviceAction;
import net.mqtts.link.mapper.device.MqttsDeviceActionMapper;
import net.mqtts.link.service.device.MqttsDeviceActionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**

* @Description:    java类作用描述

* @Author:         ShiHuan Sun

* @E-mail:          13733918655@163.com

* @Website:         http://mqtts.net

* @CreateDate:     2021/11/18$ 9:41$

* @UpdateUser:     ShiHuan Sun

* @UpdateDate:     2021/11/18$ 9:41$

* @UpdateRemark:   修改内容

* @Version:        1.0

*/
@Service
public class MqttsDeviceActionServiceImpl implements MqttsDeviceActionService{

    @Resource
    private MqttsDeviceActionMapper mqttsDeviceActionMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return mqttsDeviceActionMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(MqttsDeviceAction record) {
        return mqttsDeviceActionMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(MqttsDeviceAction record) {
        return mqttsDeviceActionMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(MqttsDeviceAction record) {
        return mqttsDeviceActionMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertOrUpdateWithBLOBs(MqttsDeviceAction record) {
        return mqttsDeviceActionMapper.insertOrUpdateWithBLOBs(record);
    }

    @Override
    public int insertSelective(MqttsDeviceAction record) {
        return mqttsDeviceActionMapper.insertSelective(record);
    }

    @Override
    public MqttsDeviceAction selectByPrimaryKey(Long id) {
        return mqttsDeviceActionMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(MqttsDeviceAction record) {
        return mqttsDeviceActionMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKeyWithBLOBs(MqttsDeviceAction record) {
        return mqttsDeviceActionMapper.updateByPrimaryKeyWithBLOBs(record);
    }

    @Override
    public int updateByPrimaryKey(MqttsDeviceAction record) {
        return mqttsDeviceActionMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<MqttsDeviceAction> list) {
        return mqttsDeviceActionMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<MqttsDeviceAction> list) {
        return mqttsDeviceActionMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<MqttsDeviceAction> list) {
        return mqttsDeviceActionMapper.batchInsert(list);
    }

}
