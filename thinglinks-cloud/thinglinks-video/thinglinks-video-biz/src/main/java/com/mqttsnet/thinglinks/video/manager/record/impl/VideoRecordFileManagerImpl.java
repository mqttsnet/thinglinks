package com.mqttsnet.thinglinks.video.manager.record.impl;

import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordFile;
import com.mqttsnet.thinglinks.video.manager.record.VideoRecordFileManager;
import com.mqttsnet.thinglinks.video.mapper.record.VideoRecordFileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * 录像文件 Manager 实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoRecordFileManagerImpl extends SuperManagerImpl<VideoRecordFileMapper, VideoRecordFile> implements VideoRecordFileManager {

    @Override
    public List<VideoRecordFile> listByDeviceAndChannel(String deviceIdentification, String channelIdentification) {
        QueryWrap<VideoRecordFile> queryWrap = new QueryWrap<>();
        queryWrap.lambda()
                .eq(VideoRecordFile::getDeviceIdentification, deviceIdentification)
                .eq(VideoRecordFile::getChannelIdentification, channelIdentification)
                .orderByDesc(VideoRecordFile::getStartTime);
        return list(queryWrap);
    }

    @Override
    public List<VideoRecordFile> listByPlanId(Long planId) {
        QueryWrap<VideoRecordFile> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(VideoRecordFile::getPlanId, planId);
        return list(queryWrap);
    }

    @Override
    public List<VideoRecordFile> listByDate(String deviceIdentification, String channelIdentification, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        QueryWrap<VideoRecordFile> queryWrap = new QueryWrap<>();
        queryWrap.lambda()
                .eq(VideoRecordFile::getDeviceIdentification, deviceIdentification)
                .eq(VideoRecordFile::getChannelIdentification, channelIdentification)
                .ge(VideoRecordFile::getStartTime, dayStart)
                .le(VideoRecordFile::getStartTime, dayEnd)
                .orderByAsc(VideoRecordFile::getStartTime);
        return list(queryWrap);
    }

    @Override
    public List<LocalDate> getRecordDates(String deviceIdentification, String channelIdentification, YearMonth month) {
        LocalDateTime monthStart = month.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = month.atEndOfMonth().atTime(LocalTime.MAX);
        QueryWrap<VideoRecordFile> queryWrap = new QueryWrap<>();
        queryWrap.lambda()
                .eq(VideoRecordFile::getDeviceIdentification, deviceIdentification)
                .eq(VideoRecordFile::getChannelIdentification, channelIdentification)
                .ge(VideoRecordFile::getStartTime, monthStart)
                .le(VideoRecordFile::getStartTime, monthEnd)
                .select(VideoRecordFile::getStartTime);
        return list(queryWrap).stream()
                .map(f -> f.getStartTime().toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
