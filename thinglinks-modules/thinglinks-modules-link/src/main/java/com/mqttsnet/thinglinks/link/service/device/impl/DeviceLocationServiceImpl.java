package com.mqttsnet.thinglinks.link.service.device.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceLocation;
import java.util.List;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceLocationMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceLocationService;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/26$ 0:27$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/26$ 0:27$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Service
public class DeviceLocationServiceImpl implements DeviceLocationService {

    @Resource
    private DeviceLocationMapper deviceLocationMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return deviceLocationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(DeviceLocation record) {
        return deviceLocationMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(DeviceLocation record) {
        return deviceLocationMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(DeviceLocation record) {
        return deviceLocationMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(DeviceLocation record) {
        return deviceLocationMapper.insertSelective(record);
    }

    @Override
    public DeviceLocation selectByPrimaryKey(Long id) {
        return deviceLocationMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(DeviceLocation record) {
        return deviceLocationMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(DeviceLocation record) {
        return deviceLocationMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<DeviceLocation> list) {
        return deviceLocationMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<DeviceLocation> list) {
        return deviceLocationMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<DeviceLocation> list) {
        return deviceLocationMapper.batchInsert(list);
    }

}

