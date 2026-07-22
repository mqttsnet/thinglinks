package com.mqttsnet.thinglinks.video.isup.event;

import com.mqttsnet.thinglinks.video.isup.event.source.IsupAlarmReceivedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * ISUP 告警接收事件。
 * 当 ISUP 设备上报告警信息时发布。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class IsupAlarmReceivedEvent extends ApplicationEvent {

    private final IsupAlarmReceivedEventSource eventSource;

    /**
     * 构造 ISUP 告警接收事件。
     *
     * @param source 事件源
     */
    public IsupAlarmReceivedEvent(IsupAlarmReceivedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
