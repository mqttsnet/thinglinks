package com.mqttsnet.thinglinks.broker.common.counter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.thinglinks.common.cache.link.counter.DownLinkDataCounterCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 下行数据下发计数器 ── 由 broker 自维护.
 * <p>命令 / 消息下发(MQTT、WebSocket)按"天-分钟"计数:hash key = {@code yyyyMMdd},
 * field = {@code HHmm},value 自增;租户隔离与 90 天过期由
 * {@link DownLinkDataCounterCacheKeyBuilder} 内部完成(取当前 {@code ContextUtil} 租户,
 * 与资产统计读取侧同租户)。资产统计的上下行明细据此 hGetAll 读取。
 *
 * <p>计数键由 broker 自行维护(下发的主要发生地),直接使用 cache-starter 的 {@link CachePlusOps}
 * 与 common 的 KEY 构造器,不跨模块借用 link 的缓存助手 ── 与上行计数口径一致(谁产生谁计数)。
 *
 * @author mqttsnet
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DownLinkDataReportCounter {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern(DateUtils.YYYYMMDD_FORMAT);
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern(DateUtils.HHMM_FORMAT);

    private final CachePlusOps cachePlusOps;

    /**
     * 下行数据下发计数 +1 ── 命令 / 消息下发时调用.
     * <p>计数为旁路统计,失败仅告警不抛,绝不影响消息下发主链路.
     */
    public void incrementDownLink() {
        try {
            LocalDateTime now = LocalDateTime.now();
            CacheHashKey hashKey = DownLinkDataCounterCacheKeyBuilder.build(now.format(DAY_FORMATTER), now.format(MINUTE_FORMATTER));
            cachePlusOps.incrementHashCounter(hashKey);
        } catch (Exception e) {
            log.warn("[DownLinkDataReportCounter] downlink counter increment failed", e);
        }
    }
}
