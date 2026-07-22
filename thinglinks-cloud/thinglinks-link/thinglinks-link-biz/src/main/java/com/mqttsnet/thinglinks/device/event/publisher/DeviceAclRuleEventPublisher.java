package com.mqttsnet.thinglinks.device.event.publisher;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.device.event.DeviceAclRuleChangedEvent;
import com.mqttsnet.thinglinks.device.event.source.DeviceAclRuleChangedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * ACL 规则事件发布器。
 *
 * @author mqttsnet
 * @since 2026-05-28
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceAclRuleEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布 ACL 规则变更事件 ── 写完后调,事务提交后触发缓存失效。
     *
     * @param source ACL 规则变更事件源
     */
    public void publishDeviceAclRuleChangedEvent(DeviceAclRuleChangedEventSource source) {
        log.info("Publishing DeviceAclRuleChanged event: {}", JSON.toJSONString(source));
        eventPublisher.publishEvent(new DeviceAclRuleChangedEvent(source));
    }
}
