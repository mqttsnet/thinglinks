package com.mqttsnet.thinglinks.video.manager.device;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.device.VideoNotifySubscription;

import java.util.List;

/**
 * Description:
 * 通知订阅 Manager 接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
public interface VideoNotifySubscriptionManager extends SuperManager<VideoNotifySubscription> {

    /**
     * 查询匹配指定事件类型的启用订阅列表
     */
    List<VideoNotifySubscription> findMatchingByEventType(String eventType);
}
