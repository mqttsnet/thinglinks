package com.mqttsnet.thinglinks.link.service.device.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceInfoMapper;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceInfo;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;

/**
 * @Description: 子设备档案接口实现
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/4/25$ 12:44$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/25$ 12:44$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Service
public class DeviceInfoServiceImpl implements DeviceInfoService {

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return deviceInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(DeviceInfo record) {
        return deviceInfoMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(DeviceInfo record) {
        return deviceInfoMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(DeviceInfo record) {
        return deviceInfoMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(DeviceInfo record) {
        return deviceInfoMapper.insertSelective(record);
    }

    @Override
    public DeviceInfo selectByPrimaryKey(Long id) {
        return deviceInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(DeviceInfo record) {
        return deviceInfoMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(DeviceInfo record) {
        return deviceInfoMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<DeviceInfo> list) {
        return deviceInfoMapper.updateBatch(list);
    }

    @Override
    public int batchInsert(List<DeviceInfo> list) {
        return deviceInfoMapper.batchInsert(list);
    }

	@Override
	public int deleteByDeviceId(String deviceId){
		 return deviceInfoMapper.deleteByDeviceId(deviceId);
	}

	@Override
	public DeviceInfo findOneByDeviceId(String deviceId){
		 return deviceInfoMapper.findOneByDeviceId(deviceId);
	}







}


