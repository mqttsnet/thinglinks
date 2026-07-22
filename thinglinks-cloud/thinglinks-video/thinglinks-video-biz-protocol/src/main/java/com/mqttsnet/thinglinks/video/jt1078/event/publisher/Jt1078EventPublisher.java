package com.mqttsnet.thinglinks.video.jt1078.event.publisher;

import com.mqttsnet.thinglinks.video.jt1078.event.Jt1078DeviceOfflineEvent;
import com.mqttsnet.thinglinks.video.jt1078.event.Jt1078DeviceOnlineEvent;
import com.mqttsnet.thinglinks.video.jt1078.event.Jt1078StreamReadyEvent;
import com.mqttsnet.thinglinks.video.jt1078.event.source.Jt1078DeviceOfflineEventSource;
import com.mqttsnet.thinglinks.video.jt1078.event.source.Jt1078DeviceOnlineEventSource;
import com.mqttsnet.thinglinks.video.jt1078.event.source.Jt1078StreamReadyEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * JT/T 1078 事件发布器。
 * 发布终端上线、终端离线、流就绪等事件，
 * 与 GB28181 事件体系完全隔离。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Jt1078EventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布终端上线事件。
     *
     * @param source 事件源
     */
    public void publishDeviceOnlineEvent(Jt1078DeviceOnlineEventSource source) {
        log.debug("[JT1078] 发布终端上线事件: simNumber={}, plateNumber={}",
                source.getSimNumber(), source.getPlateNumber());
        eventPublisher.publishEvent(new Jt1078DeviceOnlineEvent(source));
    }

    /**
     * 发布终端离线事件。
     *
     * @param source 事件源
     */
    public void publishDeviceOfflineEvent(Jt1078DeviceOfflineEventSource source) {
        log.debug("[JT1078] 发布终端离线事件: simNumber={}, reason={}",
                source.getSimNumber(), source.getReason());
        eventPublisher.publishEvent(new Jt1078DeviceOfflineEvent(source));
    }

    /**
     * 发布流就绪事件。
     *
     * @param source 事件源
     */
    public void publishStreamReadyEvent(Jt1078StreamReadyEventSource source) {
        log.debug("[JT1078] 发布流就绪事件: simNumber={}, channelNo={}, app={}, stream={}",
                source.getSimNumber(), source.getChannelNo(), source.getApp(), source.getStream());
        eventPublisher.publishEvent(new Jt1078StreamReadyEvent(source));
    }
}
