package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.PlaybackControlEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 回放控制事件。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class PlaybackControlEvent extends ApplicationEvent {
    private final PlaybackControlEventSource eventSource;

    public PlaybackControlEvent(PlaybackControlEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
