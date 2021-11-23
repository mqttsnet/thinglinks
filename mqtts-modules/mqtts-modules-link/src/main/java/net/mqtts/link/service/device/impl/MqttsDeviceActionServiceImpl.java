package net.mqtts.link.service.device.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;
import net.mqtts.link.api.domain.MqttsDevice;
import net.mqtts.link.api.domain.MqttsDeviceAction;
import net.mqtts.link.mapper.device.MqttsDeviceActionMapper;
import net.mqtts.link.service.device.MqttsDeviceActionService;
import net.mqtts.link.service.device.MqttsDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: mqtt上下线动作数据处理
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://mqtts.net
 * @CreateDate: 2021/11/18$ 9:41$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/18$ 9:41$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Slf4j
@Service
public class MqttsDeviceActionServiceImpl implements MqttsDeviceActionService {

    @Resource
    private MqttsDeviceActionMapper mqttsDeviceActionMapper;

    @Autowired
    private MqttsDeviceService mqttsDeviceService;

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
    public List<MqttsDeviceAction> selectMqttsDeviceActionList(MqttsDeviceAction record) {
        return mqttsDeviceActionMapper.selectMqttsDeviceActionList(record);
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

    @Override
    public int deleteMqttsDeviceActionByIds(Long[] ids) {
        return mqttsDeviceActionMapper.deleteMqttsDeviceActionByIds(ids);
    }

    /**
     * 设备连接事件
     *
     * @param mqttsMessage
     */
    @Override
    public void connectEvent(String mqttsMessage) {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        map = gson.fromJson(mqttsMessage, map.getClass());
        MqttsDeviceAction mqttsDeviceAction = new MqttsDeviceAction();
        mqttsDeviceAction.setDevice_id(String.valueOf(map.get("clientIdentifier")));
        mqttsDeviceAction.setAction_type(String.valueOf(map.get("channelStatus")));
        mqttsDeviceAction.setStatus("success");
        mqttsDeviceAction.setMessage("Device Connection");
        mqttsDeviceAction.setCreate_time(LocalDateTimeUtil.now());
        mqttsDeviceActionMapper.insertOrUpdate(mqttsDeviceAction);
    }

    /**
     * 设备断开事件
     *
     * @param mqttsMessage
     */
    @Override
    public void closeEvent(String mqttsMessage) {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        map = gson.fromJson(mqttsMessage, map.getClass());
        int i = mqttsDeviceService.updateConnectStatusByClientId("OFFLINE", String.valueOf(map.get("clientIdentifier")));
        MqttsDeviceAction mqttsDeviceAction = new MqttsDeviceAction();
        mqttsDeviceAction.setDevice_id(String.valueOf(map.get("clientIdentifier")));
        mqttsDeviceAction.setAction_type(String.valueOf(map.get("channelStatus")));
        mqttsDeviceAction.setStatus(i!=0?"success":"failure");
        mqttsDeviceAction.setMessage("Device Disconnection");
        mqttsDeviceAction.setCreate_time(LocalDateTimeUtil.now());
        mqttsDeviceActionMapper.insertOrUpdate(mqttsDeviceAction);

    }

}
