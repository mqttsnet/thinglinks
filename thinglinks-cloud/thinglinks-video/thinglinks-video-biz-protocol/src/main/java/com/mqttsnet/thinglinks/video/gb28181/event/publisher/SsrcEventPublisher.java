package com.mqttsnet.thinglinks.video.gb28181.event.publisher;

import com.mqttsnet.thinglinks.video.gb28181.event.SsrcAllocatedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.SsrcPoolExhaustedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.SsrcReleasedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SsrcAllocatedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SsrcPoolExhaustedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SsrcReleasedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * SSRC 事件发布器。
 * 发布 SSRC 相关事件：分配成功、释放、池耗尽。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsrcEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布 SSRC 分配成功事件
     *
     * @param source 事件源
     */
    public void publishSsrcAllocatedEvent(SsrcAllocatedEventSource source) {
        log.debug("发布SSRC分配事件: mediaIdentification={}, ssrc={}, deviceIdentification={}",
                source.getMediaIdentification(), source.getSsrc(), source.getDeviceIdentification());
        eventPublisher.publishEvent(new SsrcAllocatedEvent(source));
    }

    /**
     * 发布 SSRC 释放事件
     *
     * @param source 事件源
     */
    public void publishSsrcReleasedEvent(SsrcReleasedEventSource source) {
        log.debug("发布SSRC释放事件: mediaIdentification={}, ssrc={}", source.getMediaIdentification(), source.getSsrc());
        eventPublisher.publishEvent(new SsrcReleasedEvent(source));
    }

    /**
     * 发布 SSRC 池耗尽事件
     *
     * @param source 事件源
     */
    public void publishSsrcPoolExhaustedEvent(SsrcPoolExhaustedEventSource source) {
        log.warn("发布SSRC池耗尽事件: mediaIdentification={}, poolSize={}", source.getMediaIdentification(), source.getPoolSize());
        eventPublisher.publishEvent(new SsrcPoolExhaustedEvent(source));
    }
}
