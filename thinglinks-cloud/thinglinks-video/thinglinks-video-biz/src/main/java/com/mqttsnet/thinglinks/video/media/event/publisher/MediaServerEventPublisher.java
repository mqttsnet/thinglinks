package com.mqttsnet.thinglinks.video.media.event.publisher;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.video.media.event.MediaServerCapabilityDetectedEvent;
import com.mqttsnet.thinglinks.video.media.event.MediaServerRegisteredEvent;
import com.mqttsnet.thinglinks.video.media.event.source.MediaServerCapabilityDetectedEventSource;
import com.mqttsnet.thinglinks.video.media.event.source.MediaServerRegisteredEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 流媒体服务器事件发布器。
 * 发布流媒体服务器相关事件，包括服务器注册和能力检测完成。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaServerEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布流媒体服务器注册事件
     *
     * @param source 事件源
     */
    public void publishMediaServerRegisteredEvent(MediaServerRegisteredEventSource source) {
        log.info("发布流媒体服务器注册事件: {}", JSON.toJSONString(source));
        eventPublisher.publishEvent(new MediaServerRegisteredEvent(source));
    }

    /**
     * 发布流媒体服务器能力检测完成事件
     *
     * @param source 事件源
     */
    public void publishMediaServerCapabilityDetectedEvent(MediaServerCapabilityDetectedEventSource source) {
        log.info("发布流媒体服务器能力检测事件: mediaIdentification={}, capabilities={}",
                source.getMediaIdentification(), source.getCapabilities());
        eventPublisher.publishEvent(new MediaServerCapabilityDetectedEvent(source));
    }
}
