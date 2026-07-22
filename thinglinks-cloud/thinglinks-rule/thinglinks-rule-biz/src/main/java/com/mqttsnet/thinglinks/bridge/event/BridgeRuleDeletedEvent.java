package com.mqttsnet.thinglinks.bridge.event;

import com.mqttsnet.thinglinks.bridge.event.source.BridgeRuleChangedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 桥接规则删除事件 ── 与 {@link BridgeRuleChangedEvent}(create/update/enable/disable)分开,
 * 名字上区分变更 vs 删除,语义清晰。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
public class BridgeRuleDeletedEvent extends ApplicationEvent {

    private final BridgeRuleChangedEventSource eventSource;

    public BridgeRuleDeletedEvent(BridgeRuleChangedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
