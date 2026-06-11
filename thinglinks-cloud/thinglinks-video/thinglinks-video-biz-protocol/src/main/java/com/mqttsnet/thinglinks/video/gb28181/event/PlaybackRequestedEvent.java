package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.PlaybackRequestedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 回放请求事件。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class PlaybackRequestedEvent extends ApplicationEvent {
    private final PlaybackRequestedEventSource eventSource;

    public PlaybackRequestedEvent(PlaybackRequestedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
