package com.mqttsnet.thinglinks.bridge.policy;

import com.mqttsnet.thinglinks.cache.vo.bridge.DataBridgeCacheVO;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataSourceCacheVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 桥接重试 / 限流策略解析器,三层 fallback:rule 字段 → dataSource.default_* → 代码常量。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Component
@Slf4j
public class BridgeRetryPolicyResolver {

    private static final int FALLBACK_QOS = 1;
    private static final int FALLBACK_RATE_LIMIT = 0;
    private static final int FALLBACK_RETRY_MAX = 3;
    private static final long FALLBACK_RETRY_BACKOFF = 1000L;
    private static final int FALLBACK_TIMEOUT = 5000;

    public BridgeRetryPolicy resolve(DataBridgeCacheVO rule, DataSourceCacheVO ds) {
        return BridgeRetryPolicy.builder()
                .qos(coalesceInt(rule.getQos(), ds.getDefaultQos(), FALLBACK_QOS))
                .rateLimitQps(coalesceInt(rule.getRateLimitQps(), ds.getDefaultRateLimitQps(), FALLBACK_RATE_LIMIT))
                .retryMaxTimes(coalesceInt(rule.getRetryMaxTimes(), ds.getDefaultRetryMaxTimes(), FALLBACK_RETRY_MAX))
                .retryBackoffMs(coalesceLong(rule.getRetryBackoffMs(), ds.getDefaultRetryBackoffMs(), FALLBACK_RETRY_BACKOFF))
                .timeoutMs(coalesceInt(rule.getTimeoutMs(), ds.getDefaultTimeoutMs(), FALLBACK_TIMEOUT))
                .deadLetterDataSourceId(coalesceNullable(rule.getDeadLetterDataSourceId(),
                        ds.getDefaultDeadLetterDataSourceId()))
                .build();
    }

    private static int coalesceInt(Integer a, Integer b, int fallback) {
        return Optional.ofNullable(a).or(() -> Optional.ofNullable(b)).orElse(fallback);
    }

    private static long coalesceLong(Integer a, Integer b, long fallback) {
        return Optional.ofNullable(a)
                .map(Integer::longValue)
                .or(() -> Optional.ofNullable(b).map(Integer::longValue))
                .orElse(fallback);
    }

    private static Long coalesceNullable(Long a, Long b) {
        return Optional.ofNullable(a).or(() -> Optional.ofNullable(b)).orElse(null);
    }
}
