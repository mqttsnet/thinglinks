package com.mqttsnet.thinglinks.video.isup.event;

import com.mqttsnet.thinglinks.video.isup.event.source.IsupDeviceOnlineEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * ISUP 设备上线事件。
 * 当 ISUP 设备主动注册上线时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class IsupDeviceOnlineEvent extends ApplicationEvent {

    private final IsupDeviceOnlineEventSource eventSource;

    /**
     * 构造 ISUP 设备上线事件。
     *
     * @param source 事件源
     */
    public IsupDeviceOnlineEvent(IsupDeviceOnlineEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
