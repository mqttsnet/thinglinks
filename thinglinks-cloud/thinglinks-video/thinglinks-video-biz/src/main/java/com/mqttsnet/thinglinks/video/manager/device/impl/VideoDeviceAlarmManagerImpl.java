package com.mqttsnet.thinglinks.video.manager.device.impl;

import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceAlarm;
import com.mqttsnet.thinglinks.video.manager.device.VideoDeviceAlarmManager;
import com.mqttsnet.thinglinks.video.mapper.device.VideoDeviceAlarmMapper;
import com.mqttsnet.thinglinks.video.vo.result.device.AlarmStatisticsResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Description:
 * 设备告警 Manager 实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceAlarmManagerImpl extends SuperManagerImpl<VideoDeviceAlarmMapper, VideoDeviceAlarm> implements VideoDeviceAlarmManager {

    @Override
    public List<VideoDeviceAlarm> listByDeviceId(String deviceIdentification) {
        QueryWrap<VideoDeviceAlarm> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(VideoDeviceAlarm::getDeviceIdentification, deviceIdentification)
                .orderByDesc(VideoDeviceAlarm::getAlarmTime);
        return list(queryWrap);
    }

    @Override
    public List<AlarmStatisticsResultVO.CountItem> countByPriority(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.countByPriority(startTime, endTime);
    }

    @Override
    public List<AlarmStatisticsResultVO.CountItem> countByDevice(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.countByDevice(startTime, endTime);
    }

    @Override
    public List<AlarmStatisticsResultVO.CountItem> countByDay(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.countByDay(startTime, endTime);
    }

    @Override
    public Long countUnhandled() {
        return baseMapper.countUnhandled();
    }
}
