package com.mqttsnet.thinglinks.bridge.event;

import com.mqttsnet.thinglinks.bridge.event.source.RuleGroovyScriptChangedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 规则脚本删除事件 ── 与 {@link RuleGroovyScriptChangedEvent} 分开。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
public class RuleGroovyScriptDeletedEvent extends ApplicationEvent {

    private final RuleGroovyScriptChangedEventSource eventSource;

    public RuleGroovyScriptDeletedEvent(RuleGroovyScriptChangedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
