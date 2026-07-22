package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.StreamReadyEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 流就绪事件。
 * 当 RTP 流到达流媒体服务器并可播放时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class StreamReadyEvent extends ApplicationEvent {
    private final StreamReadyEventSource eventSource;

    public StreamReadyEvent(StreamReadyEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
