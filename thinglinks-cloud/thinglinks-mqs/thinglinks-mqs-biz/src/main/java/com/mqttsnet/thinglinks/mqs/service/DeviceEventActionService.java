package com.mqttsnet.thinglinks.mqs.service;

import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;

/**
 * ============================================================================
 * Description:
 * 设备事件动作Service
 * ============================================================================
 *
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

    /**
     * 统一落库设备动作 ── 由 dispatcher 收口调用,直接取 {@link CommonDeviceEvent} 字段建表。
     * facade 失败仅告警不抛(旁路审计,不阻断主链路)。
     *
     * @param event 设备通用事件
     */
    void save(CommonDeviceEvent event);
}
