package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.DeviceControlExecutedEventSource;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 设备控制执行事件。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public class DeviceControlExecutedEvent extends ApplicationEvent {

    private final DeviceControlExecutedEventSource eventSource;

    public DeviceControlExecutedEvent(Object source, DeviceControlExecutedEventSource eventSource) {
        super(source);
        this.eventSource = eventSource;
    }

    public DeviceControlExecutedEventSource getEventSource() {
        return eventSource;
    }
}
