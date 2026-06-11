package com.mqttsnet.thinglinks.video.isup.sdk;

/**
 * Description:
 * ISUP 告警事件回调接口。
 * 当设备产生告警事件时由 SDK 适配层触发回调。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@FunctionalInterface
public interface IsupAlarmCallback {

    /**
     * 告警事件回调。
     *
     * @param deviceSerial 设备序列号
     * @param channelNo    通道号
     * @param alarmType    告警类型
     * @param alarmData    告警附加数据（JSON 格式）
     */
    void onAlarm(String deviceSerial, Integer channelNo, String alarmType, String alarmData);
}
