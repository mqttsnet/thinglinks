package net.mqtts.link.service.device.impl;

import net.mqtts.link.api.domain.MqttsDeviceDatas;
import net.mqtts.link.mapper.device.MqttsDeviceDatasMapper;
import net.mqtts.link.service.device.MqttsDeviceDatasService;
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
public class MqttsDeviceDatasServiceImpl implements MqttsDeviceDatasService{

    @Resource
    private MqttsDeviceDatasMapper mqttsDeviceDatasMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return mqttsDeviceDatasMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(MqttsDeviceDatas record) {
        return mqttsDeviceDatasMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(MqttsDeviceDatas record) {
        return mqttsDeviceDatasMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(MqttsDeviceDatas record) {
        return mqttsDeviceDatasMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertOrUpdateWithBLOBs(MqttsDeviceDatas record) {
        return mqttsDeviceDatasMapper.insertOrUpdateWithBLOBs(record);
    }

    @Override
    public int insertSelective(MqttsDeviceDatas record) {
        return mqttsDeviceDatasMapper.insertSelective(record);
    }

    @Override
    public List<MqttsDeviceDatas> selectMqttsDeviceDatasList(MqttsDeviceDatas record) {
        return mqttsDeviceDatasMapper.selectMqttsDeviceDatasList(record);
    }

    @Override
    public MqttsDeviceDatas selectByPrimaryKey(Long id) {
        return mqttsDeviceDatasMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(MqttsDeviceDatas record) {
        return mqttsDeviceDatasMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKeyWithBLOBs(MqttsDeviceDatas record) {
        return mqttsDeviceDatasMapper.updateByPrimaryKeyWithBLOBs(record);
    }

    @Override
    public int updateByPrimaryKey(MqttsDeviceDatas record) {
        return mqttsDeviceDatasMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<MqttsDeviceDatas> list) {
        return mqttsDeviceDatasMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<MqttsDeviceDatas> list) {
        return mqttsDeviceDatasMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<MqttsDeviceDatas> list) {
        return mqttsDeviceDatasMapper.batchInsert(list);
    }

    @Override
    public int deleteMqttsDeviceDatasByIds(Long[] ids) {
        return mqttsDeviceDatasMapper.deleteMqttsDeviceDatasByIds(ids);
    }

}
