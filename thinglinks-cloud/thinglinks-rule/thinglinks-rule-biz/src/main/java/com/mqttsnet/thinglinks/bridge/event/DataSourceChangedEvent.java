package com.mqttsnet.thinglinks.bridge.event;

import com.mqttsnet.thinglinks.bridge.event.source.DataSourceChangedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 数据源变更事件(create/update/enable/disable)。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
public class DataSourceChangedEvent extends ApplicationEvent {

    private final DataSourceChangedEventSource eventSource;

    public DataSourceChangedEvent(DataSourceChangedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
