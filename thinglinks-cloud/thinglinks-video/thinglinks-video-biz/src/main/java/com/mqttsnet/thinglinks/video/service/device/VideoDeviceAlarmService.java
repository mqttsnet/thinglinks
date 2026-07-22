package com.mqttsnet.thinglinks.video.service.device;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceAlarm;
import com.mqttsnet.thinglinks.video.vo.result.device.AlarmStatisticsResultVO;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceAlarmResultVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Description:
 * 设备告警业务接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface VideoDeviceAlarmService extends SuperService<Long, VideoDeviceAlarm> {

    VideoDeviceAlarm saveAlarm(VideoDeviceAlarm alarm);

    List<VideoDeviceAlarmResultVO> listByDeviceId(String deviceIdentification);

    void updateHandleStatus(Long alarmId, Integer handleStatus);

    void clearAlarmByDeviceId(String deviceIdentification);

    /**
     * 确认告警（待处理→处理中）
     *
     * @param alarmId      告警 ID
     * @param userId       处理人 ID
     * @param handleResult 处理说明（必填，审计用）
     */
    void confirmAlarm(Long alarmId, Long userId, String handleResult);

    /**
     * 解决告警（处理中→已处理）
     */
    void resolveAlarm(Long alarmId, String handleResult);

    /**
     * 忽略告警（待处理→已忽略）
     */
    void ignoreAlarm(Long alarmId, String handleResult);

    /**
     * 批量忽略
     */
    void batchIgnore(List<Long> alarmIds);

    /**
     * 告警统计
     */
    AlarmStatisticsResultVO statistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 未处理告警计数
     */
    Long unhandledCount();
}
