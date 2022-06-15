package com.mqttsnet.thinglinks.link.service.device.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceTopicMapper;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceTopic;
import java.util.List;
import com.mqttsnet.thinglinks.link.service.device.DeviceTopicService;
/**
* @Description: java类作用描述
* @Author: ShiHuan SUN
* @E-mail: 13733918655@163.com
* @Website: http://thinglinks.mqttsnet.com
* @CreateDate: 2022/6/15$ 15:22$
* @UpdateUser: ShiHuan SUN
* @UpdateDate: 2022/6/15$ 15:22$
* @UpdateRemark: 修改内容
* @Version: V1.0
*/
@Service
public class DeviceTopicServiceImpl implements DeviceTopicService{

    @Resource
    private DeviceTopicMapper deviceTopicMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return deviceTopicMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(DeviceTopic record) {
        return deviceTopicMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(DeviceTopic record) {
        return deviceTopicMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(DeviceTopic record) {
        return deviceTopicMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(DeviceTopic record) {
        return deviceTopicMapper.insertSelective(record);
    }

    @Override
    public DeviceTopic selectByPrimaryKey(Long id) {
        return deviceTopicMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(DeviceTopic record) {
        return deviceTopicMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(DeviceTopic record) {
        return deviceTopicMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<DeviceTopic> list) {
        return deviceTopicMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<DeviceTopic> list) {
        return deviceTopicMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<DeviceTopic> list) {
        return deviceTopicMapper.batchInsert(list);
    }

}
