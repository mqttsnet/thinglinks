package com.mqttsnet.thinglinks.mqs.event.counter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.thinglinks.common.cache.link.counter.UpLinkDataCounterCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 上行数据上报计数器 ── 由 mqs 自维护.
 * <p>设备 PUBLISH(上行数据上报)动作按"天-分钟"计数:hash key = {@code yyyyMMdd},
 * field = {@code HHmm},value 自增;租户隔离与 90 天过期由
 * {@link UpLinkDataCounterCacheKeyBuilder} 内部完成。资产统计的上下行明细据此 hGetAll 读取。
 *
 * <p>计数键由 mqs 自行维护(数据上报的主要发生地),直接使用 cache-starter 的 {@link CachePlusOps}
 * 与 common 的 KEY 构造器,不跨模块借用 link 的缓存助手。
 *
 * @author mqttsnet
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LinkDataReportCounter {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern(DateUtils.YYYYMMDD_FORMAT);
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern(DateUtils.HHMM_FORMAT);

    private final CachePlusOps cachePlusOps;

    /**
     * 上行数据上报计数 +1 ── 设备 PUBLISH 业务数据上报时调用.
     * <p>计数为旁路统计,失败仅告警不抛,绝不影响数据上报主链路.
     */
    public void incrementUpLink() {
        try {
            LocalDateTime now = LocalDateTime.now();
            CacheHashKey hashKey = UpLinkDataCounterCacheKeyBuilder.build(now.format(DAY_FORMATTER), now.format(MINUTE_FORMATTER));
            cachePlusOps.incrementHashCounter(hashKey);
        } catch (Exception e) {
            log.warn("[LinkDataReportCounter] uplink counter increment failed", e);
        }
    }
}
