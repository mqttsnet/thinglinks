package com.mqttsnet.thinglinks.video.manager.device;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceAlarm;
import com.mqttsnet.thinglinks.video.vo.result.device.AlarmStatisticsResultVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Description:
 * 设备告警 Manager 接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface VideoDeviceAlarmManager extends SuperManager<VideoDeviceAlarm> {

    List<VideoDeviceAlarm> listByDeviceId(String deviceIdentification);

    List<AlarmStatisticsResultVO.CountItem> countByPriority(LocalDateTime startTime, LocalDateTime endTime);

    List<AlarmStatisticsResultVO.CountItem> countByDevice(LocalDateTime startTime, LocalDateTime endTime);

    List<AlarmStatisticsResultVO.CountItem> countByDay(LocalDateTime startTime, LocalDateTime endTime);

    Long countUnhandled();
}
