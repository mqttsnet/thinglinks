package com.mqttsnet.thinglinks.bridge.event.listener;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.bridge.event.SubscriptionSourceChangedEvent;
import com.mqttsnet.thinglinks.bridge.event.source.SubscriptionSourceChangedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 订阅源变更 Redis Pub/Sub 接收方:收到远端节点变更消息后转发为本地 {@link SubscriptionSourceChangedEvent},驱动本节点 SubscriptionSourceLifecycleManager 的 @EventListener start/stop 本地 Source。
 * 关键约束:Redis 订阅线程是 lettuce/jedis 内部线程池无 LocalMap,必须从事件读出 tenantId 写回 ContextUtil,下游 startOne 的 Service 才能 @DS 切对应租户库;且只发本地事件不回写 Redis,防回声循环。
 * 反序列化目标用 plain POJO {@link SubscriptionSourceChangedEventSource} 而非 ApplicationEvent(后者自带 source/timestamp 且无空构造,不便走 JSON)。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionSourceRedisListener {

    private final ApplicationEventPublisher eventPublisher;

    public void onRedisMessage(byte[] body, byte[] channel) {
        if (body == null || body.length == 0) {
            return;
        }
        SubscriptionSourceChangedEventSource source;
        try {
            String json = new String(body, StandardCharsets.UTF_8);
            // 使用 fastjson2 ── hutool JSONUtil 在 hutool-json/hutool-core 子包间存在
            // CopyOptions.setFormatIfDate 等 ABI 漂移,运行时易 NoSuchMethodError
            source = JSON.parseObject(json, SubscriptionSourceChangedEventSource.class);
        } catch (Exception e) {
            log.warn("[SubscriptionSourceRedisListener] failed to deserialize Redis pub-sub message", e);
            return;
        }

        Long tenantId = source.getTenantId();
        log.info("[SubscriptionSourceRedisListener] received Redis pub-sub channel={} op={} sourceCode={} tenantId={}",
                channel == null ? "?" : new String(channel, StandardCharsets.UTF_8),
                source.getOperation(), source.getSourceCode(), tenantId);

        // Redis 订阅线程没有租户上下文,需要把事件里的 tenantId 写回 ContextUtil,
        // 下游 SubscriptionSourceLifecycleManager.startOne 调 dataSourceService.getById
        // 才能让 @DS(BASE_TENANT) 正确切到对应租户库,否则会读默认库导致拉不到数据。
        boolean tenantApplied = false;
        if (tenantId != null) {
            ContextUtil.setTenantId(tenantId);
            tenantApplied = true;
        } else {
            log.warn("[SubscriptionSourceRedisListener] event has no tenantId, skip publish to avoid wrong DB sourceCode={}",
                    source.getSourceCode());
            return;
        }
        try {
            eventPublisher.publishEvent(new SubscriptionSourceChangedEvent(source));
        } catch (Exception e) {
            log.warn("[SubscriptionSourceRedisListener] failed to publish local event op={} sourceCode={}",
                    source.getOperation(), source.getSourceCode(), e);
        } finally {
            if (tenantApplied) {
                ContextUtil.remove();
            }
        }
    }
}
