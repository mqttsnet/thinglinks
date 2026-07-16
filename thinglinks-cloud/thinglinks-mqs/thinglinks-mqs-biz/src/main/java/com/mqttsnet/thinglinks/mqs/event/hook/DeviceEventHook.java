package com.mqttsnet.thinglinks.mqs.event.hook;

import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import org.springframework.core.Ordered;

/**
 * 设备事件钩子 ── {@code DeviceEventDispatcher} 在 Processor 主路径前后顺序回调.
 * <p>
 * 跨协议生效(MQTT / WS / TCP 等);任一 hook 失败强隔离,不影响其他 hook 与主链路.
 * 顺序见 {@link DeviceEventHookOrder}.
 *
 * @author mqttsnet
 * @since 2026-05-14
 */
public interface DeviceEventHook extends Ordered {

    /**
     * 数字越小越优先 ── {@code DeviceEventDispatcher.init} 用本值排序 hook 列表.
     * <p>实现建议返回 {@link DeviceEventHookOrder} 枚举对应值,集中管理顺序避免散落硬编码.
     *
     * @return 顺序值,默认 0
     */
    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * 是否对当前事件感兴趣 ── dispatcher 触发本 hook 前的过滤入口.
     * <p>默认转调 {@link #supports(DeviceActionTypeEnum)},仅按 actionType 过滤;
     * 若需按 clientId / topic 等细粒度过滤可覆盖本方法.
     *
     * @param ctx 当前分发上下文
     * @return 是否处理本次事件
     */
    default boolean supports(DeviceEventContext ctx) {
        return supports(ctx.getType());
    }

    /**
     * 按事件 {@link DeviceActionTypeEnum} 类型筛选 ── 默认全接.
     *
     * @param type 当前事件动作类型
     * @return 是否处理此 actionType
     */
    default boolean supports(DeviceActionTypeEnum type) {
        return true;
    }

    /**
     * Processor 主路径**之前**回调 ── 状态同步 / 入口指标 / trace 注入 等横切关注点的入口.
     * <p>抛异常由 {@code DeviceEventDispatcher.fireHooks} 强隔离,不影响其它 hook 与主链路.
     *
     * @param ctx 当前分发上下文
     */
    void beforeDispatch(DeviceEventContext ctx);

    /**
     * Processor 主路径**之后**回调 ── 收尾审计 / 出口指标等.
     * <p>默认空实现;主路径抛异常仍会触发本回调(best-effort,避免半截事件不可观测).
     *
     * @param ctx 当前分发上下文
     */
    default void afterDispatch(DeviceEventContext ctx) {
    }

    /**
     * 本 hook 自身失败时的兜底回调 ── 用于记录 metric / 触发告警 等.
     * <p>默认空实现.本回调自身抛异常也会被 dispatcher 吞掉,防止错误传播.
     *
     * @param ctx 当前分发上下文
     * @param t   触发本回调的异常
     */
    default void onError(DeviceEventContext ctx, Throwable t) {
    }
}
