package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.StreamClosedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 流关闭事件。
 * 当流被关闭时发布，携带关闭原因（正常/超时/异常）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class StreamClosedEvent extends ApplicationEvent {
    private final StreamClosedEventSource eventSource;

    public StreamClosedEvent(StreamClosedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
