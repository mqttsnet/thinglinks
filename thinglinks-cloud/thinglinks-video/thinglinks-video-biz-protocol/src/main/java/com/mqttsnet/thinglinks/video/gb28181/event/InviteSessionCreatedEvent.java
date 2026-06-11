package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.InviteSessionCreatedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * Invite 会话创建事件。
 * 当 SIP INVITE 会话创建时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class InviteSessionCreatedEvent extends ApplicationEvent {
    private final InviteSessionCreatedEventSource eventSource;

    public InviteSessionCreatedEvent(InviteSessionCreatedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
