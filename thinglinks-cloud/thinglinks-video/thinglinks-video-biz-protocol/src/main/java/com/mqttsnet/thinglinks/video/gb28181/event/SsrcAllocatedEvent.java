package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.SsrcAllocatedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * SSRC 分配成功事件。
 * 当 SSRC 从池中成功分配时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class SsrcAllocatedEvent extends ApplicationEvent {
    private final SsrcAllocatedEventSource eventSource;

    public SsrcAllocatedEvent(SsrcAllocatedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
