package com.mqttsnet.thinglinks.video.manager.device.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceMobilePosition;
import com.mqttsnet.thinglinks.video.manager.device.VideoDeviceMobilePositionManager;
import com.mqttsnet.thinglinks.video.mapper.device.VideoDeviceMobilePositionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Description:
 * 设备移动位置 Manager 实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceMobilePositionManagerImpl extends SuperManagerImpl<VideoDeviceMobilePositionMapper, VideoDeviceMobilePosition> implements VideoDeviceMobilePositionManager {

    @Override
    public VideoDeviceMobilePosition getLatestPosition(String deviceIdentification) {
        QueryWrap<VideoDeviceMobilePosition> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(VideoDeviceMobilePosition::getDeviceIdentification, deviceIdentification)
                .orderByDesc(VideoDeviceMobilePosition::getReportTime)
                .last("LIMIT 1");
        return getOne(queryWrap);
    }

    @Override
    public IPage<VideoDeviceMobilePosition> pageHistory(Page<VideoDeviceMobilePosition> page,
                                                         String deviceIdentification,
                                                         LocalDateTime startTime,
                                                         LocalDateTime endTime) {
        QueryWrap<VideoDeviceMobilePosition> queryWrap = new QueryWrap<>();
        queryWrap.lambda()
                .eq(VideoDeviceMobilePosition::getDeviceIdentification, deviceIdentification)
                .ge(ObjectUtil.isNotNull(startTime), VideoDeviceMobilePosition::getReportTime, startTime)
                .le(ObjectUtil.isNotNull(endTime), VideoDeviceMobilePosition::getReportTime, endTime)
                .orderByAsc(VideoDeviceMobilePosition::getReportTime);
        return page(page, queryWrap);
    }
}
