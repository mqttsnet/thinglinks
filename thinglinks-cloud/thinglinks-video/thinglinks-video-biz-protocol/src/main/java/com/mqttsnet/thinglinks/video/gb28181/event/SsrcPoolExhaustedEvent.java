package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.SsrcPoolExhaustedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * SSRC 池耗尽事件。
 * 当 SSRC 池中无可用 SSRC 时发布，用于告警监控。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class SsrcPoolExhaustedEvent extends ApplicationEvent {
    private final SsrcPoolExhaustedEventSource eventSource;

    public SsrcPoolExhaustedEvent(SsrcPoolExhaustedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
