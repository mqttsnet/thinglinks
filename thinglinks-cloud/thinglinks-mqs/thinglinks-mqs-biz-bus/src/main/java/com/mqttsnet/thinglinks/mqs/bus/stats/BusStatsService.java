package com.mqttsnet.thinglinks.mqs.bus.stats;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.mqs.bus.BusStatsCounterCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 协议总线统计服务,集群级指标(Redis hash bucket 跨节点天然汇总)。
 *
 * <p>Hash 桶按天分:{@code bus:{tenantId}:stats_counter:bucket:obj:{yyyyMMdd}:{dimension}}
 * → field={label_parts...} → count。TTL 7 天,前端按"今日/昨日/7日"切片直接 hGetAll。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusStatsService {

    public static final String DIM_DISPATCH_TOTAL = "dispatch_total";
    public static final String DIM_STAGE_EXECUTIONS = "stage_executions";
    public static final String DIM_NO_ROUTE = "no_route";
    public static final String DIM_CANONICALIZE_FAIL = "canonicalize_fail";
    public static final String DIM_RELAY_SEND = "relay_send";

    private static final String DATE_PATTERN = "yyyyMMdd";
    private static final String LABEL_SEP = ":";
    private static final String NULL_LABEL = "_";

    private final CachePlusOps cachePlusOps;

    /**
     * raw hash entry → {field, longCount};非数字 / null 返 empty。
     */
    private static Optional<Map.Entry<String, Long>> toEntry(Object field, CacheResult<Object> val) {
        return Optional.ofNullable(val)
            .map(CacheResult::getRawValue)
            .flatMap(v -> {
                if (v instanceof Number n) {
                    return Optional.of(n.longValue());
                }
                if (v instanceof String s) {
                    try {
                        return Optional.of(Long.parseLong(s));
                    } catch (NumberFormatException ignore) {
                        return Optional.empty();
                    }
                }
                return Optional.empty();
            })
            .map(count -> Map.entry(String.valueOf(field), count));
    }

    private static String joinLabel(String... parts) {
        if (parts == null || parts.length == 0) {
            return NULL_LABEL;
        }
        return Arrays.stream(parts)
            .map(p -> StrUtil.nullToDefault(p, NULL_LABEL))
            .collect(Collectors.joining(LABEL_SEP));
    }

    public void incrementDispatch(String protocol, String action, String group, String status) {
        increment(DIM_DISPATCH_TOTAL, joinLabel(protocol, action, group, status));
    }

    public void incrementStage(String stage, String phase, String status) {
        increment(DIM_STAGE_EXECUTIONS, joinLabel(stage, phase, status));
    }

    public void incrementNoRoute(String topic) {
        increment(DIM_NO_ROUTE, StrUtil.nullToDefault(topic, NULL_LABEL));
    }

    public void incrementCanonicalizeFail(String protocol, String topic) {
        increment(DIM_CANONICALIZE_FAIL, joinLabel(protocol, topic));
    }

    public void incrementRelay(String stage, String targetMq, String targetTopic, String status) {
        increment(DIM_RELAY_SEND, joinLabel(stage, targetMq, targetTopic, status));
    }

    /**
     * 通用计数器 hIncrBy;写入失败仅 warn。
     */
    public void increment(String dimension, String labelKey) {
        try {
            CacheHashKey key = BusStatsCounterCacheKeyBuilder.buildHashFieldKey(today(), dimension, labelKey);
            cachePlusOps.hIncrBy(key, 1L);
        } catch (Exception e) {
            log.warn("[bus.stats] increment failed dim={} label={} err={}", dimension, labelKey, e.getMessage());
        }
    }

    /**
     * 查询某天某维度的所有 label 计数。
     */
    public Map<String, Long> queryDimension(String date, String dimension) {
        try {
            String d = StrUtil.isBlank(date) ? today() : date;
            CacheKey bucketKey = BusStatsCounterCacheKeyBuilder.builder(d, dimension);
            Map<Object, CacheResult<Object>> raw = cachePlusOps.hGetAll(bucketKey);
            if (CollUtil.isEmpty(raw)) {
                return Collections.emptyMap();
            }
            return raw.entrySet().stream()
                .map(e -> toEntry(e.getKey(), e.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue,
                    (a, b) -> a, LinkedHashMap::new));
        } catch (Exception e) {
            log.warn("[bus.stats] queryDimension failed date={} dim={} err={}", date, dimension, e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Map<String, Long> queryTodayDispatchTotal() {
        return queryDimension(null, DIM_DISPATCH_TOTAL);
    }

    public Map<String, Long> queryTodayStageExecutions() {
        return queryDimension(null, DIM_STAGE_EXECUTIONS);
    }

    public Map<String, Long> queryTodayRelaySend() {
        return queryDimension(null, DIM_RELAY_SEND);
    }

    private String today() {
        return DateUtil.format(new Date(), DATE_PATTERN);
    }
}
