package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.PlayRequestedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 播放请求事件。
 * 当播放请求发起时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class PlayRequestedEvent extends ApplicationEvent {
    private final PlayRequestedEventSource eventSource;

    public PlayRequestedEvent(PlayRequestedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
