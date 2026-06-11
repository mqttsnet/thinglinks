package com.mqttsnet.thinglinks.service;

import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;

/**
 * ============================================================================
 * Description:
 * 设备事件动作Service
 * ============================================================================
 * @author mqttsnet
 */
public interface DeviceEventActionService {

    /**
     * 保存设备事件动作
     *
     * @param eventMessage 事件消息
     * @param actionType   动作类型
     * @param describable  描述
     */
    void saveDeviceEventAction(String eventMessage, DeviceActionTypeEnum actionType, String describable);
}
