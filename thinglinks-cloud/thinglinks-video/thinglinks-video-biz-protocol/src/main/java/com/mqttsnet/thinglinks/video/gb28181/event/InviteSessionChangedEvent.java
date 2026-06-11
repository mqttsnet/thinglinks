package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.InviteSessionChangedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * Invite 会话状态变更事件。
 * 当会话状态发生变更时发布（如 INVITED → CONNECTED）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class InviteSessionChangedEvent extends ApplicationEvent {
    private final InviteSessionChangedEventSource eventSource;

    public InviteSessionChangedEvent(InviteSessionChangedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
