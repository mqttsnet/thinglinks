package com.mqttsnet.thinglinks.video.isup.event;

import com.mqttsnet.thinglinks.video.isup.event.source.IsupStreamReadyEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * ISUP 流就绪事件。
 * 当 ISUP 设备视频流可播放时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class IsupStreamReadyEvent extends ApplicationEvent {

    private final IsupStreamReadyEventSource eventSource;

    /**
     * 构造 ISUP 流就绪事件。
     *
     * @param source 事件源
     */
    public IsupStreamReadyEvent(IsupStreamReadyEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
