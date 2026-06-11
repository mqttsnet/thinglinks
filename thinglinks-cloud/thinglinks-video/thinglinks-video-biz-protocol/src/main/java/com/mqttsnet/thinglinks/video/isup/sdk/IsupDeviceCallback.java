package com.mqttsnet.thinglinks.video.isup.sdk;

/**
 * Description:
 * ISUP 设备事件回调接口。
 * 当设备上线、离线等事件发生时由 SDK 适配层触发回调。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@FunctionalInterface
public interface IsupDeviceCallback {

    /**
     * 设备事件回调。
     *
     * @param deviceSerial 设备序列号
     * @param eventType    事件类型（如 ONLINE、OFFLINE）
     * @param eventData    事件附加数据（JSON 格式）
     */
    void onDeviceEvent(String deviceSerial, String eventType, String eventData);
}
