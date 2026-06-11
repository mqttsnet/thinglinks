package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.RtpPortReleasedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * RTP 端口释放事件。
 * 当 RTP 端口归还到池中时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class RtpPortReleasedEvent extends ApplicationEvent {
    private final RtpPortReleasedEventSource eventSource;

    public RtpPortReleasedEvent(RtpPortReleasedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
