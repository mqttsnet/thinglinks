package com.mqttsnet.thinglinks.link.service.device.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceActionMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceActionService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 设备动作数据Service业务层处理
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
public class DeviceActionServiceImpl implements DeviceActionService {

    @Resource
    private DeviceActionMapper deviceActionMapper;
    @Autowired
    private DeviceService deviceService;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return deviceActionMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(DeviceAction record) {
        return deviceActionMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(DeviceAction record) {
        return deviceActionMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(DeviceAction record) {
        return deviceActionMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertOrUpdateWithBLOBs(DeviceAction record) {
        return deviceActionMapper.insertOrUpdateWithBLOBs(record);
    }

    @Override
    public int insertSelective(DeviceAction record) {
        return deviceActionMapper.insertSelective(record);
    }

    @Override
    public DeviceAction selectByPrimaryKey(Long id) {
        return deviceActionMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(DeviceAction record) {
        return deviceActionMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKeyWithBLOBs(DeviceAction record) {
        return deviceActionMapper.updateByPrimaryKeyWithBLOBs(record);
    }

    @Override
    public int updateByPrimaryKey(DeviceAction record) {
        return deviceActionMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<DeviceAction> list) {
        return deviceActionMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<DeviceAction> list) {
        return deviceActionMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<DeviceAction> list) {
        return deviceActionMapper.batchInsert(list);
    }

    /**
     * 设备连接事件
     *
     * @param thinglinksMessage
     */
    @Override
    public void connectEvent(String thinglinksMessage) {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        map = gson.fromJson(thinglinksMessage, map.getClass());
        int i = deviceService.updateConnectStatusByClientId("ONLINE", String.valueOf(map.get("clientIdentifier")));
        DeviceAction deviceAction = new DeviceAction();
        deviceAction.setDeviceIdentification(String.valueOf(map.get("clientIdentifier")));
        deviceAction.setActionType(String.valueOf(map.get("channelStatus")));
        deviceAction.setStatus("success");
        deviceAction.setMessage("Device Connection");
        deviceAction.setCreateTime(LocalDateTimeUtil.now());
        deviceActionMapper.insert(deviceAction);
    }

    /**
     * 设备断开事件
     *
     * @param thinglinksMessage
     */
    @Override
    public void closeEvent(String thinglinksMessage) {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        map = gson.fromJson(thinglinksMessage, map.getClass());
        int i = deviceService.updateConnectStatusByClientId("OFFLINE", String.valueOf(map.get("clientIdentifier")));
        DeviceAction deviceAction = new DeviceAction();
        deviceAction.setDeviceIdentification(String.valueOf(map.get("clientIdentifier")));
        deviceAction.setActionType(String.valueOf(map.get("channelStatus")));
        deviceAction.setStatus(i != 0 ? "success" : "failure");
        deviceAction.setMessage("Device Disconnection");
        deviceAction.setCreateTime(LocalDateTimeUtil.now());
        deviceActionMapper.insert(deviceAction);
    }


    /**
     * 查询设备动作数据
     *
     * @param id 设备动作数据主键
     * @return 设备动作数据
     */
    @Override
    public DeviceAction selectDeviceActionById(Long id)
    {
        return deviceActionMapper.selectDeviceActionById(id);
    }

    /**
     * 查询设备动作数据列表
     *
     * @param deviceAction 设备动作数据
     * @return 设备动作数据
     */
    @Override
    public List<DeviceAction> selectDeviceActionList(DeviceAction deviceAction)
    {
        return deviceActionMapper.selectDeviceActionList(deviceAction);
    }

    /**
     * 新增设备动作数据
     *
     * @param deviceAction 设备动作数据
     * @return 结果
     */
    @Override
    public int insertDeviceAction(DeviceAction deviceAction)
    {
        deviceAction.setCreateTime(DateUtils.dateToLocalDateTime(DateUtils.getNowDate()));
        return deviceActionMapper.insertDeviceAction(deviceAction);
    }

    /**
     * 修改设备动作数据
     *
     * @param deviceAction 设备动作数据
     * @return 结果
     */
    @Override
    public int updateDeviceAction(DeviceAction deviceAction)
    {
        return deviceActionMapper.updateDeviceAction(deviceAction);
    }

    /**
     * 批量删除设备动作数据
     *
     * @param ids 需要删除的设备动作数据主键
     * @return 结果
     */
    @Override
    public int deleteDeviceActionByIds(Long[] ids)
    {
        return deviceActionMapper.deleteDeviceActionByIds(ids);
    }

    /**
     * 删除设备动作数据信息
     *
     * @param id 设备动作数据主键
     * @return 结果
     */
    @Override
    public int deleteDeviceActionById(Long id)
    {
        return deviceActionMapper.deleteDeviceActionById(id);
    }

}


