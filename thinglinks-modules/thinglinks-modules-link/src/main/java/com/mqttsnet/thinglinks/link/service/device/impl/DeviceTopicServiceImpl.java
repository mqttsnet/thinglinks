package com.mqttsnet.thinglinks.link.service.device.impl;

import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceTopic;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceTopicMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceTopicService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
/**
* @Description: 设备Topic数据Service业务层处理
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
    @Autowired
    private TokenService tokenService;

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


    /**
     * 查询设备Topic数据
     *
     * @param id 设备Topic数据主键
     * @return 设备Topic数据
     */
    @Override
    public DeviceTopic selectDeviceTopicById(Long id)
    {
        return deviceTopicMapper.selectDeviceTopicById(id);
    }

    /**
     * 查询设备Topic数据列表
     *
     * @param deviceTopic 设备Topic数据
     * @return 设备Topic数据
     */
    @Override
    public List<DeviceTopic> selectDeviceTopicList(DeviceTopic deviceTopic)
    {
        return deviceTopicMapper.selectDeviceTopicList(deviceTopic);
    }

    /**
     * 新增设备Topic数据
     *
     * @param deviceTopic 设备Topic数据
     * @return 结果
     */
    @Override
    public int insertDeviceTopic(DeviceTopic deviceTopic)
    {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        deviceTopic.setCreateBy(sysUser.getUserName());
        return deviceTopicMapper.insertDeviceTopic(deviceTopic);
    }

    /**
     * 修改设备Topic数据
     *
     * @param deviceTopic 设备Topic数据
     * @return 结果
     */
    @Override
    public int updateDeviceTopic(DeviceTopic deviceTopic)
    {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        deviceTopic.setUpdateBy(sysUser.getUserName());
        return deviceTopicMapper.updateDeviceTopic(deviceTopic);
    }

    /**
     * 批量删除设备Topic数据
     *
     * @param ids 需要删除的设备Topic数据主键
     * @return 结果
     */
    @Override
    public int deleteDeviceTopicByIds(Long[] ids)
    {
        return deviceTopicMapper.deleteDeviceTopicByIds(ids);
    }

    /**
     * 删除设备Topic数据信息
     *
     * @param id 设备Topic数据主键
     * @return 结果
     */
    @Override
    public int deleteDeviceTopicById(Long id)
    {
        return deviceTopicMapper.deleteDeviceTopicById(id);
    }
}
