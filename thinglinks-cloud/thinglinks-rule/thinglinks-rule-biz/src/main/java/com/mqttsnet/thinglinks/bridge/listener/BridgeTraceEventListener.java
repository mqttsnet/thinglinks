package com.mqttsnet.thinglinks.bridge.listener;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.bridge.trace.BridgeTraceCompletedEvent;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionStep;
import com.mqttsnet.thinglinks.service.bridge.BridgeExecutionStepService;
import com.mqttsnet.thinglinks.service.bridge.BridgeExecutionTraceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 桥接 trace 异步入库监听器。
 *
 * <p>{@link ContextUtil} 普通 ThreadLocal 跨线程必丢 ── 用 event 上游快照恢复 + finally remove
 * 防 worker 复用串扰。必须走 Service(非 Manager):Service 标 {@code @DS(BASE_TENANT)} AOP 切库。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BridgeTraceEventListener {

    private final BridgeExecutionTraceService traceService;
    private final BridgeExecutionStepService stepService;

    @Async("ruleBridgeLogExecutor")
    @EventListener
    public void onTraceCompleted(BridgeTraceCompletedEvent event) {
        if (event == null || event.getTrace() == null) {
            return;
        }
        ContextUtil.setLocalMap(event.getContextSnapshot());
        String traceId = event.getTrace().getTraceId();
        try {
            // saveBatch + singletonList 绕开 SuperServiceImpl.save 的 BeanUtil.toBean 转换
            traceService.saveBatch(Collections.singletonList(event.getTrace()));
            Optional.ofNullable(event.getSteps())
                    .filter(steps -> !steps.isEmpty())
                    .ifPresent(this::saveSteps);
        } catch (DuplicateKeyException dup) {
            // RocketMQ 重投 / 死信回放下,同一 traceId 二次落盘是预期(uk_trace_id 拦截)
            log.info("[BridgeTraceEventListener] trace already persisted traceId={}", traceId);
        } catch (Exception e) {
            log.warn("[BridgeTraceEventListener] persist failed (non-blocking) traceId={}", traceId, e);
        } finally {
            ContextUtil.remove();
        }
    }

    private void saveSteps(List<BridgeExecutionStep> steps) {
        stepService.saveBatch(steps);
    }
}
