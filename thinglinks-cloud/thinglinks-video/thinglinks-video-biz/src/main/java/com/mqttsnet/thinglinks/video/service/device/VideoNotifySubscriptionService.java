package com.mqttsnet.thinglinks.video.service.device;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.device.VideoNotifySubscription;

import java.util.List;

/**
 * Description:
 * 通知订阅业务接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
public interface VideoNotifySubscriptionService extends SuperService<Long, VideoNotifySubscription> {

    /**
     * 查询匹配指定事件类型的启用订阅列表
     */
    List<VideoNotifySubscription> findMatchingByEventType(String eventType);
}
