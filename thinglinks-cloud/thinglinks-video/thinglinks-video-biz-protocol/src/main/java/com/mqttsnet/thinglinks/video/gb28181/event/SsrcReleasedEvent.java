package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.SsrcReleasedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * SSRC 释放事件。
 * 当 SSRC 归还到池中时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class SsrcReleasedEvent extends ApplicationEvent {
    private final SsrcReleasedEventSource eventSource;

    public SsrcReleasedEvent(SsrcReleasedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
