package com.mqttsnet.thinglinks.device.event;

import com.mqttsnet.thinglinks.device.event.source.DeviceAclRuleChangedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * ACL 规则变更事件 ── save / update / delete 三类写操作完成后发布。
 *
 * <p>下游通过 @TransactionalEventListener(AFTER_COMMIT) 消费,
 * 触发 redis 缓存按维度失效。事务回滚则不触发,保证缓存与 DB 一致。
 *
 * @author mqttsnet
 * @since 2026-05-28
 */
@Getter
public class DeviceAclRuleChangedEvent extends ApplicationEvent {

    private final DeviceAclRuleChangedEventSource eventSource;

    public DeviceAclRuleChangedEvent(DeviceAclRuleChangedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
