package com.mqttsnet.thinglinks.mqs.bus.stage.enrich;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.cache.utils.CachePlusUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.bus.hook.DeviceEventEnricher;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.cache.link.device.DeviceCacheKeyBuilder;
import com.mqttsnet.thinglinks.constants.bus.BusConstants;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备缓存富化器,PRE 前调用,clientId 查 {@link DeviceCacheVO} 一次塞 event + ctx 供后续 stage 复用.
 * 同步 ContextUtil 让后续 stage 内 @DS(BASE_TENANT) 能切租户库.
 *
 * <p>故意只读 cache 不做 DB 回源:PRE 阶段要极低延迟,且 mqs-biz-bus 不应反向依赖 link-biz;
 * 缓存由 link 端 CRUD 主动写入并管理生命周期,mqs 仅 read-only 消费。cache miss 仅 warn 不阻塞,
 * 后续 stage 自行 null check。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceCacheEnricher implements DeviceEventEnricher {

    private final CachePlusUtil cachePlusOpsUtil;

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public void enrich(DeviceProtocolEvent event, StageContext context) {
        String clientId = event.getClientId();
        if (StrUtil.isBlank(clientId)) {
            return;
        }
        try {
            cachePlusOpsUtil.getObjectFromCache(DeviceCacheKeyBuilder.build(clientId).getKey(), DeviceCacheVO.class)
                .ifPresentOrElse(
                    cache -> apply(event, context, cache),
                    () -> log.warn("[bus.enricher.device-cache] cache miss clientId={} traceId={}", clientId, event.getTraceId()));
        } catch (Exception e) {
            log.warn("[bus.enricher.device-cache] lookup failed clientId={} traceId={} err={}", clientId, event.getTraceId(), e.getMessage());
        }
    }

    private void apply(DeviceProtocolEvent event, StageContext context, DeviceCacheVO cache) {
        fillEventFromCache(event, cache);
        // 解析出的设备缓存直接挂到事件,经 assembler 透传到 CommonDeviceEvent → 下游免重取(context 仍保留供 stage 内用)
        event.setDeviceCache(cache);
        context.put(BusConstants.Ctx.DEVICE_CACHE, cache);
        syncContextUtil(cache);
    }

    /**
     * 把 cache 字段填到 event(已有值不覆盖)。
     *
     * @param event 设备协议事件
     * @param cache 设备缓存
     */
    private void fillEventFromCache(DeviceProtocolEvent event, DeviceCacheVO cache) {
        if (StrUtil.isBlank(event.getAppId())) {
            event.setAppId(cache.getAppId());
        }
        if (StrUtil.isBlank(event.getProductIdentification())) {
            event.setProductIdentification(cache.getProductIdentification());
        }
        if (StrUtil.isBlank(event.getDeviceIdentification())) {
            event.setDeviceIdentification(cache.getDeviceIdentification());
        }
        if (StrUtil.isBlank(event.getTenantId()) && cache.getTenantId() != null) {
            event.setTenantId(String.valueOf(cache.getTenantId()));
        }
    }

    /**
     * 同步 ContextUtil 让后续 stage 内 @DS(BASE_TENANT) 能切库。
     * Kafka header 先 set 的是 broker 级 tenantId(临时),这里拿到缓存的业务平台 tenantId 后无条件覆盖,以缓存为权威。
     * {@link ContextUtil#setTenantId} 内部联动设置 tenantBasePoolName + tenantExtendPoolName,无需再单独调。
     *
     * @param cache 设备缓存
     */
    private void syncContextUtil(DeviceCacheVO cache) {
        Long tenantId = cache.getTenantId();
        if (tenantId == null) {
            return;
        }
        ContextUtil.setTenantId(tenantId);
    }
}
