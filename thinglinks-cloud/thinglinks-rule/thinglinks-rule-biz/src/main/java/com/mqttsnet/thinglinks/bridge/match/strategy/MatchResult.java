package com.mqttsnet.thinglinks.bridge.match.strategy;

import lombok.Getter;

import java.util.function.Supplier;

/**
 * 桥接规则匹配的单维度结果(三态:HIT / MISS / SKIP)。
 *
 * <p>{@link #reason} 写入 trace step,前端详情页能看到"为什么没命中"。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
@Getter
public final class MatchResult {

    public enum State { HIT, MISS, SKIP }

    private final State state;
    private final String reason;

    private MatchResult(State state, String reason) {
        this.state = state;
        this.reason = reason;
    }

    public boolean isHit()  { return state == State.HIT; }
    public boolean isMiss() { return state == State.MISS; }
    public boolean isSkip() { return state == State.SKIP; }

    public static MatchResult hit(String reason)  { return new MatchResult(State.HIT, reason); }
    public static MatchResult miss(String reason) { return new MatchResult(State.MISS, reason); }
    public static MatchResult skip()              { return new MatchResult(State.SKIP, "skip (not configured)"); }

    /**
     * 安全包装:任何异常 → fail-closed 的 MISS,reason 含异常类型。
     *
     * <p>所有 strategy 的 {@code match} 入口推荐用此方法包装,统一异常语义,避免散落 try/catch。
     *
     * @param strategyName 策略名(用于异常 reason 拼接)
     * @param body         实际匹配逻辑
     * @return body 抛异常时返回 fail-closed MISS;正常返回时透传 body 结果
     */
    public static MatchResult safe(String strategyName, Supplier<MatchResult> body) {
        try {
            MatchResult r = body.get();
            return r == null ? miss(strategyName + " returned null") : r;
        } catch (RuntimeException e) {
            return miss(strategyName + " error: " + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : " - " + e.getMessage()));
        }
    }

    @Override
    public String toString() {
        return state + (reason == null ? "" : ": " + reason);
    }
}
