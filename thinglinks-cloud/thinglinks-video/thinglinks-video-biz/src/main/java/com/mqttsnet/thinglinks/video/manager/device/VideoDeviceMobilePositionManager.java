package com.mqttsnet.thinglinks.video.manager.device;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceMobilePosition;

import java.time.LocalDateTime;

/**
 * Description:
 * 设备移动位置 Manager 接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface VideoDeviceMobilePositionManager extends SuperManager<VideoDeviceMobilePosition> {

    VideoDeviceMobilePosition getLatestPosition(String deviceIdentification);

    /**
     * 查询设备历史轨迹
     */
    IPage<VideoDeviceMobilePosition> pageHistory(Page<VideoDeviceMobilePosition> page,
                                                  String deviceIdentification,
                                                  LocalDateTime startTime,
                                                  LocalDateTime endTime);
}
