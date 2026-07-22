package com.mqttsnet.thinglinks.video.gb28181.event.publisher;

import com.mqttsnet.thinglinks.video.gb28181.event.DeviceControlExecutedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.PtzCommandExecutedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.source.DeviceControlExecutedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.PtzCommandExecutedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 设备控制事件发布者。
 * 发布 PTZ 命令执行、设备控制执行等事件。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceControlEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 发布 PTZ 命令执行事件
     */
    public void publishPtzCommandExecutedEvent(PtzCommandExecutedEventSource eventSource) {
        log.debug("发布PTZ命令执行事件: deviceIdentification={}, command={}", eventSource.getDeviceIdentification(), eventSource.getCommandType());
        applicationEventPublisher.publishEvent(new PtzCommandExecutedEvent(this, eventSource));
    }

    /**
     * 发布设备控制执行事件
     */
    public void publishDeviceControlExecutedEvent(DeviceControlExecutedEventSource eventSource) {
        log.debug("发布设备控制执行事件: deviceIdentification={}, controlType={}", eventSource.getDeviceIdentification(), eventSource.getControlType());
        applicationEventPublisher.publishEvent(new DeviceControlExecutedEvent(this, eventSource));
    }
}
