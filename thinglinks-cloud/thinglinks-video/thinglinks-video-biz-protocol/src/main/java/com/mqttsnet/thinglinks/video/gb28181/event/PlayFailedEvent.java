package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.PlayFailedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 播放失败事件。
 * 当播放发起失败时发布，携带失败原因和设备信息。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class PlayFailedEvent extends ApplicationEvent {
    private final PlayFailedEventSource eventSource;

    public PlayFailedEvent(PlayFailedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
