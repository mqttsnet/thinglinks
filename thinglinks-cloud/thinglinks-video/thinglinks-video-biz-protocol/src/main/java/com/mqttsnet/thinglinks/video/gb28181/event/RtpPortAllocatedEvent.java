package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.RtpPortAllocatedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * RTP 端口分配事件。
 * 当 RTP 端口从池中分配时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class RtpPortAllocatedEvent extends ApplicationEvent {
    private final RtpPortAllocatedEventSource eventSource;

    public RtpPortAllocatedEvent(RtpPortAllocatedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
