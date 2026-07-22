package com.mqttsnet.thinglinks.bridge.event;

import com.mqttsnet.thinglinks.bridge.event.source.SubscriptionSourceChangedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 订阅源变更事件(Spring ApplicationEvent)。
 *
 * <p>触发时机:SubscriptionSourceServiceImpl save / update / delete / changeStatus 后发布。
 * <p>消费端:{@code SubscriptionSourceLifecycleManager} 监听 → 重启 / 停止 Source 实例。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Getter
public class SubscriptionSourceChangedEvent extends ApplicationEvent {

    private final SubscriptionSourceChangedEventSource eventSource;

    public SubscriptionSourceChangedEvent(SubscriptionSourceChangedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
