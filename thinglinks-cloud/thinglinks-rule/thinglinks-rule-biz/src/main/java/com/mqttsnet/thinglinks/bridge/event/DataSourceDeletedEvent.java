package com.mqttsnet.thinglinks.bridge.event;

import com.mqttsnet.thinglinks.bridge.event.source.DataSourceChangedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 数据源删除事件 ── 与 {@link DataSourceChangedEvent} 分开。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
public class DataSourceDeletedEvent extends ApplicationEvent {

    private final DataSourceChangedEventSource eventSource;

    public DataSourceDeletedEvent(DataSourceChangedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
