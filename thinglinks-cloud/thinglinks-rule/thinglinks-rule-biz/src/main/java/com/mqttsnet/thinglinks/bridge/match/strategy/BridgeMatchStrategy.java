package com.mqttsnet.thinglinks.bridge.match.strategy;

import com.mqttsnet.thinglinks.bridge.matcher.BridgeMatchConfig;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;

/**
 * 桥接规则匹配 ── 单维度策略 SPI(策略工厂模式)。
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><b>开闭原则</b>:新增维度只加新 {@link org.springframework.stereotype.Component} 实现 +
 *       {@link org.springframework.core.annotation.Order} 注解,自动加入链,不动核心</li>
 *   <li><b>独立测试</b>:每个 strategy 是无副作用的纯函数,可单独 mock 验证</li>
 *   <li><b>失败策略闭包</b>:各维度未实现子模式的 fail-open / fail-closed 决策由策略自己负责,
 *       不让上层关心</li>
 *   <li><b>trace 友好</b>:{@link MatchResult#getReason()} 给 trace step 输出可读说明,
 *       桥接日志详情页能直观看出"为什么没命中"</li>
 * </ul>
 *
 * <h3>调用顺序</h3>
 * 通过 {@link org.springframework.core.annotation.Order} 注解或 implementing
 * {@link org.springframework.core.Ordered} 控制。{@link com.mqttsnet.thinglinks.bridge.match.BridgeMatchStrategyChain}
 * 按 order 从小到大执行,任一 strategy 返回 {@code MISS} 立即 short-circuit。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
public interface BridgeMatchStrategy {

    /**
     * 策略可读名称(写入 trace step 的 stepName)。例:{@code "PRODUCT"} / {@code "TOPIC"}。
     */
    String name();

    /**
     * 当前规则配置中是否启用本策略。
     * <p>配置里没填该维度时返回 {@code false},chain 跳过不调用 {@link #match}。
     * 这样无配置维度不浪费 CPU + 不污染 trace。
     */
    boolean appliesTo(BridgeMatchConfig cfg);

    /**
     * 执行匹配。仅在 {@link #appliesTo} 返回 true 时被 chain 调用。
     *
     * @param env envelope(只读,strategy 不应修改)
     * @param cfg 已解析的匹配配置(只读)
     * @return 匹配结果(hit / miss),不要返回 skip(skip 应通过 appliesTo 返回 false 表达)
     */
    MatchResult match(BridgeMessageEnvelope env, BridgeMatchConfig cfg);
}
