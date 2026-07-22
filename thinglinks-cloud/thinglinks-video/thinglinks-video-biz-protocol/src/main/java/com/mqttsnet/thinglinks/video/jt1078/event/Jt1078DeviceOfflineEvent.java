package com.mqttsnet.thinglinks.video.jt1078.event;

import com.mqttsnet.thinglinks.video.jt1078.event.source.Jt1078DeviceOfflineEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * JT/T 1078 终端离线事件。
 * 当车载终端断开连接或心跳超时时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class Jt1078DeviceOfflineEvent extends ApplicationEvent {
    private final Jt1078DeviceOfflineEventSource eventSource;

    /**
     * 构造终端离线事件。
     *
     * @param source 事件源
     */
    public Jt1078DeviceOfflineEvent(Jt1078DeviceOfflineEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
