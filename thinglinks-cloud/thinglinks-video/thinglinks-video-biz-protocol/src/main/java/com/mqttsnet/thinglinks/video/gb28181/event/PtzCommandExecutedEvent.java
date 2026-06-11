package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.PtzCommandExecutedEventSource;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * PTZ 命令执行事件。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public class PtzCommandExecutedEvent extends ApplicationEvent {

    private final PtzCommandExecutedEventSource eventSource;

    public PtzCommandExecutedEvent(Object source, PtzCommandExecutedEventSource eventSource) {
        super(source);
        this.eventSource = eventSource;
    }

    public PtzCommandExecutedEventSource getEventSource() {
        return eventSource;
    }
}
