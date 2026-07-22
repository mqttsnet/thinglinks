package com.mqttsnet.thinglinks.video.service.device.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.AlarmMethodEnum;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.AlarmPriorityEnum;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceAlarm;
import com.mqttsnet.thinglinks.video.manager.device.VideoDeviceAlarmManager;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceAlarmService;
import com.mqttsnet.thinglinks.video.vo.result.device.AlarmStatisticsResultVO;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceAlarmResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Description:
 * 设备告警业务实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceAlarmServiceImpl extends SuperServiceImpl<VideoDeviceAlarmManager, Long, VideoDeviceAlarm> implements VideoDeviceAlarmService {

    @Override
    public VideoDeviceAlarm saveAlarm(VideoDeviceAlarm alarm) {
        Optional.ofNullable(alarm.getHandleStatus()).orElseGet(() -> {
            alarm.setHandleStatus(0);
            return 0;
        });
        superManager.save(alarm);
        // 按 GB/T 28181-2016，AlarmType 是条件必选：仅在 AlarmMethod=2(设备报警)
        // 或 AlarmMethod=6(设备故障报警) 时必填，其他方式为空是合法的。
        // 打全 method / type / priority 便于排查，alarmType=null 不代表 bug。
        log.info("保存设备告警: deviceIdentification={}, alarmMethod={}, alarmType={}, priority={}",
                alarm.getDeviceIdentification(),
                AlarmMethodEnum.descOf(alarm.getAlarmMethod()),
                alarm.getAlarmType(),
                AlarmPriorityEnum.descOf(alarm.getAlarmPriority()));
        return alarm;
    }

    @Override
    public List<VideoDeviceAlarmResultVO> listByDeviceId(String deviceIdentification) {
        List<VideoDeviceAlarm> alarms = superManager.listByDeviceId(deviceIdentification);
        return BeanPlusUtil.toBeanList(alarms, VideoDeviceAlarmResultVO.class);
    }

    @Override
    public void updateHandleStatus(Long alarmId, Integer handleStatus) {
        Optional.ofNullable(superManager.getById(alarmId)).ifPresent(alarm -> {
            alarm.setHandleStatus(handleStatus);
            superManager.updateById(alarm);
        });
    }

    @Override
    public void clearAlarmByDeviceId(String deviceIdentification) {
        List<VideoDeviceAlarm> alarms = superManager.listByDeviceId(deviceIdentification);
        if (CollUtil.isNotEmpty(alarms)) {
            List<Long> ids = alarms.stream().map(VideoDeviceAlarm::getId).toList();
            superManager.removeByIds(ids);
            log.info("清除设备告警: deviceIdentification={}, count={}", deviceIdentification, ids.size());
        }
    }

    @Override
    public void confirmAlarm(Long alarmId, Long userId, String handleResult) {
        Optional.ofNullable(superManager.getById(alarmId))
            .filter(alarm -> Objects.equals(alarm.getHandleStatus(), 0))
            .ifPresent(alarm -> {
                alarm.setHandleStatus(1);
                alarm.setHandleUserId(userId);
                alarm.setHandleTime(LocalDateTime.now());
                if (StrUtil.isNotBlank(handleResult)) {
                    alarm.setHandleResult(handleResult);
                }
                superManager.updateById(alarm);
            });
    }

    @Override
    public void resolveAlarm(Long alarmId, String handleResult) {
        Optional.ofNullable(superManager.getById(alarmId))
            .filter(alarm -> Objects.equals(alarm.getHandleStatus(), 1))
            .ifPresent(alarm -> {
                alarm.setHandleStatus(2);
                alarm.setHandleResult(handleResult);
                alarm.setHandleTime(LocalDateTime.now());
                superManager.updateById(alarm);
            });
    }

    @Override
    public void ignoreAlarm(Long alarmId, String handleResult) {
        Optional.ofNullable(superManager.getById(alarmId))
            .filter(alarm -> Objects.equals(alarm.getHandleStatus(), 0) || Objects.equals(alarm.getHandleStatus(), 1))
            .ifPresent(alarm -> {
                alarm.setHandleStatus(3);
                alarm.setHandleResult(handleResult);
                alarm.setHandleTime(LocalDateTime.now());
                superManager.updateById(alarm);
            });
    }

    @Override
    public void batchIgnore(List<Long> alarmIds) {
        CollUtil.emptyIfNull(alarmIds).forEach(id -> ignoreAlarm(id, null));
    }

    @Override
    public AlarmStatisticsResultVO statistics(LocalDateTime startTime, LocalDateTime endTime) {
        return AlarmStatisticsResultVO.builder()
            .totalCount(superManager.count())
            .unhandledCount(superManager.countUnhandled())
            .byPriority(superManager.countByPriority(startTime, endTime))
            .byDevice(superManager.countByDevice(startTime, endTime))
            .byDay(superManager.countByDay(startTime, endTime))
            .build();
    }

    @Override
    public Long unhandledCount() {
        return superManager.countUnhandled();
    }
}
