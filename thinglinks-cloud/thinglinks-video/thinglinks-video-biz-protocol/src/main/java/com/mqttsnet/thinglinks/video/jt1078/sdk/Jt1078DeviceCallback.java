package com.mqttsnet.thinglinks.video.jt1078.sdk;

/**
 * Description:
 * JT/T 1078 设备事件回调接口。
 * 用于接收来自 JT808 网关的设备事件通知，
 * 如终端上线、离线、流状态变更等。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@FunctionalInterface
public interface Jt1078DeviceCallback {

    /**
     * 设备事件回调。
     *
     * @param simNumber 终端SIM卡号
     * @param eventType 事件类型（如 ONLINE、OFFLINE、STREAM_READY 等）
     * @param eventData 事件数据（JSON 格式）
     */
    void onDeviceEvent(String simNumber, String eventType, String eventData);
}
