package com.mqttsnet.thinglinks.video.jt1078.event;

import com.mqttsnet.thinglinks.video.jt1078.event.source.Jt1078DeviceOnlineEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * JT/T 1078 终端上线事件。
 * 当车载终端通过 JT808 网关注册上线时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class Jt1078DeviceOnlineEvent extends ApplicationEvent {
    private final Jt1078DeviceOnlineEventSource eventSource;

    /**
     * 构造终端上线事件。
     *
     * @param source 事件源
     */
    public Jt1078DeviceOnlineEvent(Jt1078DeviceOnlineEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
