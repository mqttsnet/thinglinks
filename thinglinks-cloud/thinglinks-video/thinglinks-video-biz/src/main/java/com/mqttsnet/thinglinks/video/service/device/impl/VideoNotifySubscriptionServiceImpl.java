package com.mqttsnet.thinglinks.video.service.device.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.entity.device.VideoNotifySubscription;
import com.mqttsnet.thinglinks.video.manager.device.VideoNotifySubscriptionManager;
import com.mqttsnet.thinglinks.video.service.device.VideoNotifySubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 通知订阅业务实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoNotifySubscriptionServiceImpl extends SuperServiceImpl<VideoNotifySubscriptionManager, Long, VideoNotifySubscription> implements VideoNotifySubscriptionService {

    /**
     * 更新接口不携带渠道凭证或只携带空白字符时，保留原凭证。
     */
    @Override
    protected <UpdateVO> VideoNotifySubscription updateBefore(UpdateVO updateVO) {
        VideoNotifySubscription entity = super.updateBefore(updateVO);
        if (StrUtil.isBlank(entity.getChannelConfig())) {
            entity.setChannelConfig(null);
        }
        return entity;
    }

    @Override
    public List<VideoNotifySubscription> findMatchingByEventType(String eventType) {
        return superManager.findMatchingByEventType(eventType);
    }
}
