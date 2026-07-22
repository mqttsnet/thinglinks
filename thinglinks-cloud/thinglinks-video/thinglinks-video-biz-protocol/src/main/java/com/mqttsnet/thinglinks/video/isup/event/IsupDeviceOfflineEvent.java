package com.mqttsnet.thinglinks.video.isup.event;

import com.mqttsnet.thinglinks.video.isup.event.source.IsupDeviceOfflineEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * ISUP 设备离线事件。
 * 当 ISUP 设备断开连接或心跳超时时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class IsupDeviceOfflineEvent extends ApplicationEvent {

    private final IsupDeviceOfflineEventSource eventSource;

    /**
     * 构造 ISUP 设备离线事件。
     *
     * @param source 事件源
     */
    public IsupDeviceOfflineEvent(IsupDeviceOfflineEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
