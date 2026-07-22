package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.SipCommandSentEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * SIP 命令发送成功事件。
 * 当 SIP 命令（INVITE/BYE/MESSAGE/SUBSCRIBE/INFO）成功发送时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class SipCommandSentEvent extends ApplicationEvent {
    private final SipCommandSentEventSource eventSource;

    public SipCommandSentEvent(SipCommandSentEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
