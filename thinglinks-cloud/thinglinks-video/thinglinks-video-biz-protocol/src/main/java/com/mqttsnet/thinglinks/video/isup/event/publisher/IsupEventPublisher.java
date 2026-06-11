package com.mqttsnet.thinglinks.video.isup.event.publisher;

import com.mqttsnet.thinglinks.video.isup.event.IsupAlarmReceivedEvent;
import com.mqttsnet.thinglinks.video.isup.event.IsupDeviceOfflineEvent;
import com.mqttsnet.thinglinks.video.isup.event.IsupDeviceOnlineEvent;
import com.mqttsnet.thinglinks.video.isup.event.IsupStreamReadyEvent;
import com.mqttsnet.thinglinks.video.isup.event.source.IsupAlarmReceivedEventSource;
import com.mqttsnet.thinglinks.video.isup.event.source.IsupDeviceOfflineEventSource;
import com.mqttsnet.thinglinks.video.isup.event.source.IsupDeviceOnlineEventSource;
import com.mqttsnet.thinglinks.video.isup.event.source.IsupStreamReadyEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * ISUP 事件发布器。
 * 发布设备上线、设备离线、告警接收、流就绪等 ISUP 领域事件。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IsupEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布设备上线事件。
     *
     * @param source 事件源
     */
    public void publishDeviceOnlineEvent(IsupDeviceOnlineEventSource source) {
        log.debug("发布ISUP设备上线事件: deviceSerial={}, ip={}, port={}",
                source.getDeviceSerial(), source.getHost(), source.getPort());
        eventPublisher.publishEvent(new IsupDeviceOnlineEvent(source));
    }

    /**
     * 发布设备离线事件。
     *
     * @param source 事件源
     */
    public void publishDeviceOfflineEvent(IsupDeviceOfflineEventSource source) {
        log.debug("发布ISUP设备离线事件: deviceSerial={}, reason={}",
                source.getDeviceSerial(), source.getReason());
        eventPublisher.publishEvent(new IsupDeviceOfflineEvent(source));
    }

    /**
     * 发布告警接收事件。
     *
     * @param source 事件源
     */
    public void publishAlarmReceivedEvent(IsupAlarmReceivedEventSource source) {
        log.debug("发布ISUP告警接收事件: deviceSerial={}, channelNo={}, alarmType={}",
                source.getDeviceSerial(), source.getChannelNo(), source.getAlarmType());
        eventPublisher.publishEvent(new IsupAlarmReceivedEvent(source));
    }

    /**
     * 发布流就绪事件。
     *
     * @param source 事件源
     */
    public void publishStreamReadyEvent(IsupStreamReadyEventSource source) {
        log.debug("发布ISUP流就绪事件: deviceSerial={}, channelNo={}, streamUrl={}",
                source.getDeviceSerial(), source.getChannelNo(), source.getStreamUrl());
        eventPublisher.publishEvent(new IsupStreamReadyEvent(source));
    }
}
