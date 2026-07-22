package com.mqttsnet.thinglinks.video.manager.device.impl;

import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.device.VideoNotifySubscription;
import com.mqttsnet.thinglinks.video.manager.device.VideoNotifySubscriptionManager;
import com.mqttsnet.thinglinks.video.mapper.device.VideoNotifySubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 通知订阅 Manager 实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoNotifySubscriptionManagerImpl extends SuperManagerImpl<VideoNotifySubscriptionMapper, VideoNotifySubscription> implements VideoNotifySubscriptionManager {

    @Override
    public List<VideoNotifySubscription> findMatchingByEventType(String eventType) {
        QueryWrap<VideoNotifySubscription> queryWrap = new QueryWrap<>();
        queryWrap.lambda()
                .eq(VideoNotifySubscription::getStatus, 1)
                .like(VideoNotifySubscription::getEventTypes, eventType);
        return list(queryWrap);
    }
}
