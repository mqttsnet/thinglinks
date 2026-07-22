package com.mqttsnet.thinglinks.video.jt1078.event;

import com.mqttsnet.thinglinks.video.jt1078.event.source.Jt1078StreamReadyEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * JT/T 1078 流就绪事件。
 * 当车载终端的音视频流到达流媒体服务器并可播放时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class Jt1078StreamReadyEvent extends ApplicationEvent {
    private final Jt1078StreamReadyEventSource eventSource;

    /**
     * 构造流就绪事件。
     *
     * @param source 事件源
     */
    public Jt1078StreamReadyEvent(Jt1078StreamReadyEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
