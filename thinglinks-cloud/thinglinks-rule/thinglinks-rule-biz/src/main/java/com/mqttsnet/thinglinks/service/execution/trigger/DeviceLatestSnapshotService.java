package com.mqttsnet.thinglinks.service.execution.trigger;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.rule.trigger.RuleTriggerCacheKeys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备最新物模型快照 ── 事件消费时覆盖写,供"定时规则里的属性条件"与"跨设备条件"评估取最新值。
 *
 * <p><b>乱序防回退</b>:CONCURRENTLY 消费下同设备两条消息可能乱序,写前按 {@code eventUtc}
 * 比较(GET-比较-SET,两次往返;写入竞争窗口极小,最坏丢失一次快照更新,下一条上报即恢复,
 * 对"最新值"语义可接受,不为此上 Lua)。
 *
 * @author mqttsnet
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceLatestSnapshotService {

    private final CachePlusOps cachePlusOps;

    /**
     * 快照载体(eventUtc + THING_MODEL 原始 JSON)。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatestSnapshot implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long eventUtc;
        private String payload;
    }

    /**
     * 覆盖写最新快照(eventUtc 更新才写,防乱序回退)。
     */
    public void updateLatest(String productIdentification, String deviceIdentification,
                             Long eventUtc, String payload) {
        if (StrUtil.hasBlank(productIdentification, deviceIdentification) || StrUtil.isBlank(payload)) {
            return;
        }
        long effectiveUtc = eventUtc == null ? System.currentTimeMillis() : eventUtc;
        CacheKey key = RuleTriggerCacheKeys.latestSnapshot(productIdentification, deviceIdentification);
        try {
            CacheResult<LatestSnapshot> existing = cachePlusOps.get(key);
            LatestSnapshot current = existing == null ? null : existing.getValue();
            if (current != null && current.getEventUtc() != null && current.getEventUtc() > effectiveUtc) {
                return;
            }
            cachePlusOps.set(key, new LatestSnapshot(effectiveUtc, payload));
        } catch (Exception e) {
            log.warn("[LatestSnapshot] update failed product={} device={} err={}",
                    productIdentification, deviceIdentification, e.getMessage());
        }
    }

    /**
     * 读最新快照报文 JSON;无快照返回 empty(调用方自行回退)。
     */
    public Optional<String> getLatestPayload(String productIdentification, String deviceIdentification) {
        if (StrUtil.hasBlank(productIdentification, deviceIdentification)) {
            return Optional.empty();
        }
        try {
            CacheResult<LatestSnapshot> result = cachePlusOps.get(
                    RuleTriggerCacheKeys.latestSnapshot(productIdentification, deviceIdentification));
            LatestSnapshot snapshot = result == null ? null : result.getValue();
            return snapshot == null || StrUtil.isBlank(snapshot.getPayload())
                    ? Optional.empty() : Optional.of(snapshot.getPayload());
        } catch (Exception e) {
            log.warn("[LatestSnapshot] read failed product={} device={} err={}",
                    productIdentification, deviceIdentification, e.getMessage());
            return Optional.empty();
        }
    }
}
