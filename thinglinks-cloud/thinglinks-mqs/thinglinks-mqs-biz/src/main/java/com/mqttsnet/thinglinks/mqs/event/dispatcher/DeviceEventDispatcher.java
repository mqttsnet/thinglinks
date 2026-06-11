package com.mqttsnet.thinglinks.mqs.event.dispatcher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.mqs.event.hook.DeviceEventContext;
import com.mqttsnet.thinglinks.mqs.event.hook.DeviceEventHook;
import com.mqttsnet.thinglinks.mqs.event.processor.DeviceEventProcessor;
import com.mqttsnet.thinglinks.mqs.service.DeviceEventActionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

/**
 * 设备事件领域分发器 ── 协议中立,由 bus {@code DeviceBizDispatchStage} 同线程直调(ctx 归 bus 管理,本类不增删)。
 * 编排:钩子(before) → 统一落库 → 路由 {@link DeviceEventProcessor} → 钩子(after)。
 *
 * @author mqttsnet
 * @since 2026-06-02
 */
@Component("commonDeviceEventDispatcher")
@Slf4j
@RequiredArgsConstructor
public class DeviceEventDispatcher {

    private final List<DeviceEventHook> hooks;
    private final List<DeviceEventProcessor> processors;
    private final DeviceEventActionService deviceEventActionService;

    /**
     * 启动期按 {@link org.springframework.core.Ordered} 排序钩子并打印注册清单,便于排查注入/顺序问题。
     */
    @PostConstruct
    public void init() {
        if (hooks != null && !hooks.isEmpty()) {
            AnnotationAwareOrderComparator.sort(this.hooks);
            List<String> hookSig = hooks.stream()
                .map(h -> h.getClass().getSimpleName() + "@" + h.getOrder())
                .toList();
            log.info("[DeviceEventDispatcher] {} hook(s) (in order): {}", hooks.size(), hookSig);
        }
        int procSize = processors == null ? 0 : processors.size();
        List<String> procSig = processors == null
            ? Collections.emptyList()
            : processors.stream().map(p -> p.getClass().getSimpleName()).toList();
        log.info("[DeviceEventDispatcher] {} processor(s) registered: {}", procSize, procSig);
    }

    /**
     * 领域主入口:钩子(before) → 统一落库 → 路由 processor → 钩子(after)。
     * 钩子/落库失败隔离吞;processor 异常上抛由 bus 流水线兜底(→ DLT)。
     *
     * @param event 设备通用事件
     */
    public void dispatch(CommonDeviceEvent event) {
        if (event == null || event.getActionType() == null) {
            log.warn("[DeviceEventDispatcher] null event or actionType, skip");
            return;
        }
        DeviceEventContext ctx = new DeviceEventContext(event, System.currentTimeMillis());
        fireHooks(ctx, DeviceEventHook::beforeDispatch, "beforeDispatch");
        persistIfNeeded(event);
        routeToProcessor(event);
        fireHooks(ctx, DeviceEventHook::afterDispatch, "afterDispatch");
    }

    /**
     * 统一落库 device_action ── 排除高频上报的 PUBLISH(数据已入时序库);其余动作(含 PING)均落库审计。
     * facade 失败由 service 内部吞,不阻断主链。
     *
     * @param event 设备通用事件
     */
    private void persistIfNeeded(CommonDeviceEvent event) {
        if (event.getActionType() == DeviceActionTypeEnum.PUBLISH) {
            return;
        }
        deviceEventActionService.save(event);
    }

    /**
     * 按 {@link DeviceEventProcessor#supports} 路由到唯一 processor;无命中 info 不抛
     * (状态/落库已在前置完成,如 CONNECT 无专属 processor 属正常)。
     *
     * @param event 设备通用事件
     */
    private void routeToProcessor(CommonDeviceEvent event) {
        DeviceActionTypeEnum type = event.getActionType();
        Optional<DeviceEventProcessor> hit = Optional.ofNullable(processors)
            .orElse(Collections.emptyList())
            .stream()
            .filter(p -> p.supports(type))
            .findFirst();
        if (hit.isEmpty()) {
            log.info("[DeviceEventDispatcher] no processor for actionType={}, only hooks+persist applied", type);
            return;
        }
        hit.get().process(event);
    }

    /**
     * 按顺序触发钩子的 before/after 阶段;单钩子 supports/accept/onError 任一失败均隔离(error log + 继续),
     * 不传播到主链路或其它钩子。
     *
     * @param ctx    设备事件上下文
     * @param action 钩子阶段动作
     * @param phase  阶段名(日志)
     */
    private void fireHooks(DeviceEventContext ctx,
                           BiConsumer<DeviceEventHook, DeviceEventContext> action, String phase) {
        for (DeviceEventHook hook : Optional.ofNullable(hooks).orElse(Collections.emptyList())) {
            if (!safeSupports(hook, ctx)) {
                continue;
            }
            try {
                action.accept(hook, ctx);
            } catch (Exception t) {
                log.error("[DeviceEventDispatcher] hook {} phase={} failed, isolated",
                    hook.getClass().getSimpleName(), phase, t);
                try {
                    hook.onError(ctx, t);
                } catch (Exception ignore) {
                    // hook 自身 onError 失败也吞,避免连锁
                }
            }
        }
    }

    /**
     * 调 {@link DeviceEventHook#supports};抛异常视为 false(本次跳过),避免单钩子 bug 拖垮整链。
     *
     * @param hook 钩子
     * @param ctx  设备事件上下文
     * @return 是否支持;抛异常返 false
     */
    private boolean safeSupports(DeviceEventHook hook, DeviceEventContext ctx) {
        try {
            return hook.supports(ctx);
        } catch (Exception e) {
            log.warn("[DeviceEventDispatcher] hook {} supports() threw, skip", hook.getClass().getSimpleName(), e);
            return false;
        }
    }
}
