package com.mqttsnet.thinglinks.bridge.event;

import com.mqttsnet.thinglinks.bridge.event.source.BridgeRuleChangedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 桥接规则变更事件(Spring ApplicationEvent)。
 *
 * <p>触发时机:DataBridge 创建 / 更新 / 删除 / 启停后由对应 Service 发布。
 * <p>消费端:
 * <ul>
 *   <li>{@code BridgeRuleCache} ── 失效相关 cache 条目</li>
 *   <li>未来可加:审计日志、变更通知等</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Getter
public class BridgeRuleChangedEvent extends ApplicationEvent {

    private final BridgeRuleChangedEventSource eventSource;

    public BridgeRuleChangedEvent(BridgeRuleChangedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
