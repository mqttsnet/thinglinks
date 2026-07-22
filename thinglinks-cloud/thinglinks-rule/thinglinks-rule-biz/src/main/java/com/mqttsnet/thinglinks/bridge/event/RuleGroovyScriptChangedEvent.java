package com.mqttsnet.thinglinks.bridge.event;

import com.mqttsnet.thinglinks.bridge.event.source.RuleGroovyScriptChangedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 规则脚本变更事件(create/update/enable/disable)。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
public class RuleGroovyScriptChangedEvent extends ApplicationEvent {

    private final RuleGroovyScriptChangedEventSource eventSource;

    public RuleGroovyScriptChangedEvent(RuleGroovyScriptChangedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
