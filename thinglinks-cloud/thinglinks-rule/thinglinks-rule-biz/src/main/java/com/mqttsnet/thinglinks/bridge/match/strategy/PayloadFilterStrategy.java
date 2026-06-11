package com.mqttsnet.thinglinks.bridge.match.strategy;

import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mqttsnet.thinglinks.bridge.matcher.BridgeMatchConfig;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 内容(payload)维度策略 ── SPEL 已支持,JSON_PATH / GROOVY 暂未实现 fail-closed。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
@Slf4j
@Component
@Order(BridgeMatchOrder.PAYLOAD)
public class PayloadFilterStrategy implements BridgeMatchStrategy {

    private static final String STRATEGY = "PAYLOAD";

    private static final SpelExpressionParser SPEL = new SpelExpressionParser();

    private final Cache<String, Expression> spelCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    /**
     * type → handler 不可变映射(case-insensitive)
     */
    private final Map<Type, BiFunction<String, String, MatchResult>> handlers = Map.of(
            Type.SPEL,      this::matchBySpel,
            Type.JSON_PATH, (expr, payload) -> failClosed(Type.JSON_PATH),
            Type.GROOVY,    (expr, payload) -> failClosed(Type.GROOVY)
    );

    @Override
    public String name() {
        return STRATEGY;
    }

    @Override
    public boolean appliesTo(BridgeMatchConfig cfg) {
        return Optional.ofNullable(cfg)
                .flatMap(BridgeMatchConfig::payloadFilterOpt)
                .map(f -> StrUtil.isNotBlank(f.getType()) && StrUtil.isNotBlank(f.getExpression()))
                .orElse(false);
    }

    @Override
    public MatchResult match(BridgeMessageEnvelope env, BridgeMatchConfig cfg) {
        return MatchResult.safe(STRATEGY, () -> {
            BridgeMatchConfig.PayloadFilter filter = cfg.getPayloadFilter();
            Type type = Type.parse(filter.getType());
            if (type == null) {
                log.warn("[PayloadFilterStrategy] unknown type={}, fail-closed (supported: {})",
                        filter.getType(), Type.supported());
                return MatchResult.miss("unknown payloadFilter type: " + filter.getType());
            }
            String payload = Optional.ofNullable(env)
                    .map(BridgeMessageEnvelope::getRawMessage)
                    .orElse("");
            return handlers.get(type).apply(filter.getExpression(), payload);
        });
    }

    // ============================== SPEL ==============================

    private MatchResult matchBySpel(String expression, String payload) {
        try {
            Expression expr = spelCache.get(expression, SPEL::parseExpression);
            if (expr == null) {
                return MatchResult.miss("SpEL parse returned null");
            }
            String safe = StrUtil.nullToEmpty(payload);
            StandardEvaluationContext ctx = new StandardEvaluationContext(safe);
            ctx.setVariable("payload", safe);
            Boolean r = expr.getValue(ctx, Boolean.class);
            return Boolean.TRUE.equals(r)
                    ? MatchResult.hit("SPEL true")
                    : MatchResult.miss("SPEL false");
        } catch (RuntimeException e) {
            log.warn("[PayloadFilterStrategy] SPEL eval failed expression={} err={}",
                    expression, e.getMessage());
            return MatchResult.miss("SPEL eval error: " + e.getMessage());
        }
    }

    private MatchResult failClosed(Type type) {
        log.warn("[PayloadFilterStrategy] {} 未实现,fail-closed 引导用户改用 SPEL", type);
        return MatchResult.miss(type + " not yet implemented; use SPEL");
    }

    // ============================== Type 枚举 ==============================

    /**
     * payload 过滤类型枚举 ── 集中管理可识别的 type 字符串。
     * 前端规约:统一发大写(SPEL / JSON_PATH / GROOVY)。
     */
    public enum Type {
        SPEL, JSON_PATH, GROOVY;

        /**
         * 严格匹配解析;未知或非大写值返回 null。
         */
        public static Type parse(String s) {
            if (s == null || s.isBlank()) {
                return null;
            }
            try {
                return Type.valueOf(s.trim());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public static String supported() {
            return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
        }
    }
}
