package com.mqttsnet.thinglinks.video.service.device;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceMobilePosition;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceMobilePositionResultVO;

import java.time.LocalDateTime;

/**
 * Description:
 * 设备移动位置业务接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface VideoDeviceMobilePositionService extends SuperService<Long, VideoDeviceMobilePosition> {

    VideoDeviceMobilePosition savePosition(VideoDeviceMobilePosition position);

    VideoDeviceMobilePositionResultVO getLatestPosition(String deviceIdentification);

    void subscribe(String deviceIdentification, int interval);

    void unsubscribe(String deviceIdentification);

    /**
     * 查询设备历史轨迹（分页）
     */
    IPage<VideoDeviceMobilePositionResultVO> pageHistory(Page<VideoDeviceMobilePosition> page,
                                                          String deviceIdentification,
                                                          LocalDateTime startTime,
                                                          LocalDateTime endTime);
}
