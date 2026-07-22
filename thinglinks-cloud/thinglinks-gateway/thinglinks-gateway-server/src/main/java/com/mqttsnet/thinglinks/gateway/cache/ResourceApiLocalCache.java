package com.mqttsnet.thinglinks.gateway.cache;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.thinglinks.system.facade.DefResourceFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 网关资源 API 本地缓存 ── 服务就绪后预热、定时后台刷新,请求侧零 Redis。
 * 刷新失败保留上次快照,缓存空时降级直查 {@link DefResourceFacade#listAllApi()},永不返回 null(无数据时为空 Map)。
 *
 * @author mqttsnet
 * @since 2026-06-02
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceApiLocalCache {

    private final DefResourceFacade defResourceFacade;
    private final AtomicReference<Map<String, Set<String>>> ref = new AtomicReference<>(Collections.emptyMap());

    /**
     * 服务完全就绪后预热,避免启动期远程调用;失败仅告警,请求侧有降级兜底。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        refresh();
    }

    /**
     * 每 {@code gateway.resource-api.refresh-ms}(默认 60s)后台刷新,独立线程不碰事件循环。
     */
    @Scheduled(fixedDelayString = "${gateway.resource-api.refresh-ms:60000}",
        initialDelayString = "${gateway.resource-api.refresh-ms:60000}")
    public void refresh() {
        try {
            Map<String, Set<String>> latest = defResourceFacade.listAllApi();
            if (latest != null && !latest.isEmpty()) {
                ref.set(latest);
            }
        } catch (Exception e) {
            log.warn("[gateway.auth] resource-api refresh failed, keep last snapshot", e);
        }
    }

    /**
     * 取全量"接口→权限码"表;缓存空(冷启/持续失败)时降级直查,永不返回 null(无数据时为空 Map)。
     *
     * @return 接口({@code uri###method})到权限码集合的映射
     */
    public Map<String, Set<String>> getAllApi() {
        Map<String, Set<String>> cached = ref.get();
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        Map<String, Set<String>> direct = defResourceFacade.listAllApi();
        return CollUtil.isNotEmpty(direct) ? direct : Collections.emptyMap();
    }
}
