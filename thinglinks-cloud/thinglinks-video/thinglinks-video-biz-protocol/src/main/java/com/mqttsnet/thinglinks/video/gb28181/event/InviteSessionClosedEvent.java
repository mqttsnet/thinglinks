package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.InviteSessionClosedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * Invite 会话关闭事件。
 * 当会话关闭并释放资源后发布（携带关闭原因）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class InviteSessionClosedEvent extends ApplicationEvent {
    private final InviteSessionClosedEventSource eventSource;

    public InviteSessionClosedEvent(InviteSessionClosedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
