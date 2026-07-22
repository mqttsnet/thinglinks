package com.mqttsnet.thinglinks.bridge.match.strategy;

import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mqttsnet.thinglinks.bridge.matcher.BridgeMatchConfig;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * 时间窗口策略 ── Spring {@link CronExpression}(无外部依赖)解析 cron + Caffeine 缓存编译结果。
 *
 * <p><b>语义</b>:最近 60 秒内是否有 fire 时间 = 在窗口内。配 {@code "0 0 9-18 ? * MON-FRI"}
 * 后,任何在工作日 9-18 整点后 60s 内的 envelope 都命中。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
@Slf4j
@Component
@Order(BridgeMatchOrder.TIME_WINDOW)
public class TimeWindowStrategy implements BridgeMatchStrategy {

    private static final String STRATEGY = "TIME_WINDOW";
    /**
     * 窗口容错(秒)── cron 触发时刻往前推此值视为"在窗口内"。
     */
    private static final int TOLERANCE_SECONDS = 60;

    private final Cache<String, CronExpression> cronCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    @Override
    public String name() {
        return STRATEGY;
    }

    @Override
    public boolean appliesTo(BridgeMatchConfig cfg) {
        return Optional.ofNullable(cfg)
                .flatMap(BridgeMatchConfig::timeWindowOpt)
                .map(BridgeMatchConfig.TimeWindow::getCronExpr)
                .filter(StrUtil::isNotBlank)
                .isPresent();
    }

    @Override
    public MatchResult match(BridgeMessageEnvelope env, BridgeMatchConfig cfg) {
        return MatchResult.safe(STRATEGY, () -> {
            String cronExpr = cfg.getTimeWindow().getCronExpr().trim();
            CronExpression expr;
            try {
                expr = cronCache.get(cronExpr, CronExpression::parse);
            } catch (RuntimeException e) {
                log.warn("[TimeWindowStrategy] cron parse failed cronExpr={} err={}",
                        cronExpr, e.getMessage());
                return MatchResult.miss("cron parse error: " + e.getMessage());
            }
            if (expr == null) {
                return MatchResult.miss("cron parse returned null");
            }
            LocalDateTime now = resolveNow(env);
            LocalDateTime probe = now.minusSeconds(TOLERANCE_SECONDS);
            LocalDateTime next = expr.next(probe);
            if (next == null) {
                return MatchResult.miss("cron has no next fire time after " + probe);
            }
            return !next.isAfter(now)
                    ? MatchResult.hit("in window (last fire: " + next + ")")
                    : MatchResult.miss("not in window (next fire: " + next + ")");
        });
    }

    /**
     * 取 envelope.ts(毫秒)对应的 LocalDateTime;ts 缺失走系统当前时间。
     * <p>用 system default zone ── 多租户跨时区暂不支持。
     */
    private LocalDateTime resolveNow(BridgeMessageEnvelope env) {
        return Optional.ofNullable(env)
                .map(BridgeMessageEnvelope::getTs)
                .filter(ts -> ts > 0L)
                .map(ts -> LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()))
                .orElseGet(LocalDateTime::now);
    }
}
