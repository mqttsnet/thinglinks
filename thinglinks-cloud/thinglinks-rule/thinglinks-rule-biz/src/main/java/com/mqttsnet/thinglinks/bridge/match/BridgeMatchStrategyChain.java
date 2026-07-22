package com.mqttsnet.thinglinks.bridge.match;

import com.mqttsnet.thinglinks.bridge.match.strategy.BridgeMatchStrategy;
import com.mqttsnet.thinglinks.bridge.match.strategy.MatchResult;
import com.mqttsnet.thinglinks.bridge.matcher.BridgeMatchConfig;
import com.mqttsnet.thinglinks.bridge.trace.BridgeStepType;
import com.mqttsnet.thinglinks.bridge.trace.BridgeTraceBuilder;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 桥接匹配策略链 ── Spring 注入所有 {@link BridgeMatchStrategy} 按 {@code @Order} 升序;
 * 任一 strategy 抛异常 → fail-closed MISS;任一 MISS → 整链 short-circuit。
 *
 * <p>线程安全:策略链启动后只读,匹配过程无状态。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
@Slf4j
@Component
public class BridgeMatchStrategyChain {

    /**
     * 策略链(按 @Order 升序),启动后不可变。
     */
    private volatile List<BridgeMatchStrategy> strategies = List.of();

    @Autowired
    public void setStrategies(List<BridgeMatchStrategy> all) {
        List<BridgeMatchStrategy> sorted = Optional.ofNullable(all).orElseGet(List::of).stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .toList();
        this.strategies = sorted;
        log.info("[BridgeMatchStrategyChain] {} strategies registered: {}",
                sorted.size(), sorted.stream().map(BridgeMatchStrategy::name).toList());
    }

    /**
     * 纯匹配(热路径不写 trace)。任一 applicable 策略 MISS → false。
     */
    public boolean match(BridgeMessageEnvelope env, BridgeMatchConfig cfg) {
        if (cfg == null) {
            return false;
        }
        return strategies.stream()
                .filter(s -> s.appliesTo(cfg))
                .map(s -> MatchResult.safe(s.name(), () -> s.match(env, cfg)))
                .noneMatch(MatchResult::isMiss);
    }

    /**
     * 带 trace 输出版本 ── 每个 applicable 策略写一条 RULE_MATCH step。
     */
    public boolean matchWithTrace(BridgeMessageEnvelope env, BridgeMatchConfig cfg,
                                  BridgeTraceBuilder trace) {
        if (cfg == null) {
            return false;
        }
        if (trace == null) {
            return match(env, cfg);
        }
        for (BridgeMatchStrategy s : strategies) {
            if (!s.appliesTo(cfg)) {
                continue;
            }
            long start = System.currentTimeMillis();
            MatchResult r = MatchResult.safe(s.name(), () -> s.match(env, cfg));
            recordTraceStep(trace, s.name(), r, System.currentTimeMillis() - start);
            if (r.isMiss()) {
                return false;
            }
        }
        return true;
    }

    private void recordTraceStep(BridgeTraceBuilder trace, String strategyName,
                                 MatchResult r, long latency) {
        Map<String, Object> ext = new HashMap<>();
        ext.put("strategy", strategyName);
        ext.put("hit", r.isHit());
        if (r.isHit()) {
            trace.addSuccessStep(BridgeStepType.RULE_MATCH, strategyName,
                    null, r.getReason(), latency, ext);
        } else {
            trace.addFailedStep(BridgeStepType.RULE_MATCH, strategyName,
                    null, r.getReason(), latency, ext);
        }
    }
}
