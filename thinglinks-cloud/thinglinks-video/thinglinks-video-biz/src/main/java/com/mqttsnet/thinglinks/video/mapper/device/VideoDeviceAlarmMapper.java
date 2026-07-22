package com.mqttsnet.thinglinks.video.mapper.device;

import com.mqttsnet.basic.base.mapper.SuperMapper;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceAlarm;
import com.mqttsnet.thinglinks.video.vo.result.device.AlarmStatisticsResultVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Description:
 * 设备告警 Mapper 接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Repository
public interface VideoDeviceAlarmMapper extends SuperMapper<VideoDeviceAlarm> {

    List<AlarmStatisticsResultVO.CountItem> countByPriority(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<AlarmStatisticsResultVO.CountItem> countByDevice(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<AlarmStatisticsResultVO.CountItem> countByDay(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    Long countUnhandled();
}
