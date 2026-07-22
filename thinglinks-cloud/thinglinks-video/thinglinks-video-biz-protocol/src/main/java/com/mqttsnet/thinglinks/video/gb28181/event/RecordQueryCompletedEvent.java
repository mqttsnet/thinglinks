package com.mqttsnet.thinglinks.video.gb28181.event;

import com.mqttsnet.thinglinks.video.gb28181.event.source.RecordQueryCompletedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 录像查询完成事件。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class RecordQueryCompletedEvent extends ApplicationEvent {
    private final RecordQueryCompletedEventSource eventSource;

    public RecordQueryCompletedEvent(RecordQueryCompletedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
