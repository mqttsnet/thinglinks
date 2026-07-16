package com.mqttsnet.thinglinks.service.execution.trigger;

import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.dto.linkage.AntiShakeSchemePolicyDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 事件驱动防抖计数器 ── 替代"数据收集池 + 窗口过滤"的轻量实现。
 *
 * <p>语义与轮询版 {@code AntiShakeSchemeEvaluatorService} 对齐:窗口 T 秒内事件数 ≥ count
 * 才放行,取值按 occurrence.first(窗口首条报文)/ last(当前报文);
 * <b>达标放行后计数清零重开窗</b>(防同窗口连续重复触发,已与产品语义对齐)。
 *
 * <p>成本:每事件 1 次 INCR(首事件多 1 次 EXPIRE + 1 次首值快照 SET),
 * 仅对配了防抖的条件发生;没配防抖的条件零 Redis 操作。
 *
 * @author mqttsnet
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntiShakeCounterService {

    private final CachePlusOps cachePlusOps;

    /**
     * 窗口计数并按 first/last 语义取值。
     *
     * @param counterKey     计数器 key(TTL=窗口)
     * @param firstKey       首值快照 key(TTL=窗口)
     * @param policy         防抖策略(frequency.timeValue 秒窗口 / frequency.count 次数门槛 / occurrence)
     * @param currentPayload 当前报文 JSON(THING_MODEL)
     * @return 达标时返回选中的报文 JSON(first→窗口首条,last→当前);未达标返回 empty
     */
    public Optional<String> countAndSelect(CacheKey counterKey, CacheKey firstKey,
                                           AntiShakeSchemePolicyDTO policy, String currentPayload) {
        Long count = cachePlusOps.incr(counterKey);
        if (count == null) {
            log.warn("[AntiShakeCounter] incr returned null, key={}", counterKey.getKey());
            return Optional.empty();
        }
        if (count == 1L) {
            // 窗口首事件:落 TTL + 存首值快照(即使 occurrence=last 也存,策略可能被改为 first)
            cachePlusOps.expire(counterKey);
            cachePlusOps.set(firstKey, currentPayload);
        }
        // 次数门槛下限 1:配置缺失/0/负值一律按 1 处理(即"到达即触发"),避免 count 永远达不到
        int threshold = policy.getFrequency() == null || policy.getFrequency().getCount() == null
                ? 1 : Math.max(1, policy.getFrequency().getCount());
        if (count < threshold) {
            return Optional.empty();
        }

        String selected = currentPayload;
        boolean useFirst = policy.getOccurrence() != null && Boolean.TRUE.equals(policy.getOccurrence().getFirst());
        if (useFirst) {
            CacheResult<String> first = cachePlusOps.get(firstKey);
            String firstPayload = first == null ? null : first.getValue();
            if (StrUtil.isNotBlank(firstPayload)) {
                selected = firstPayload;
            }
        }
        // 达标即重开窗:清计数与首值,下一窗口重新累计
        cachePlusOps.del(counterKey, firstKey);
        return Optional.of(selected);
    }
}
