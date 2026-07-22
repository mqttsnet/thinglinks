package com.mqttsnet.thinglinks.mqs.bus.dispatcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.bus.hook.DeviceEventCallback;
import com.mqttsnet.thinglinks.bus.hook.DeviceEventDropException;
import com.mqttsnet.thinglinks.bus.hook.DeviceEventEnricher;
import com.mqttsnet.thinglinks.bus.hook.DeviceEventInterceptor;
import com.mqttsnet.thinglinks.bus.route.RouteEntry;
import com.mqttsnet.thinglinks.bus.stage.DeviceEventStage;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.constants.bus.BusConstants;
import com.mqttsnet.thinglinks.dto.bus.DeviceEventOutcome;
import com.mqttsnet.thinglinks.dto.bus.StageRecord;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.PipelineStatusEnum;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import com.mqttsnet.thinglinks.mqs.bus.core.route.TopicRouteResolver;
import com.mqttsnet.thinglinks.mqs.bus.core.stage.DeviceEventStageRegistry;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备事件调度器 ── 协议总线主入口,跑 PRE → CORE → POST 三相管道。
 *
 * <p>失败语义:PRE/CORE 失败终止主链路,POST 仍 best-effort 触发避免故障半径放大。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BusPipelineDispatcher {

    private static final String FALLBACK = "_";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String INTERNAL = "INTERNAL";

    /**
     * 走 CORE 的业务分组;其它分组跳 CORE 直接 POST 异步。
     */
    private static final Set<DispatchGroupEnum> CORE_GROUPS = Set.of(
        DispatchGroupEnum.DEVICE_DATA, DispatchGroupEnum.DEVICE_LIFECYCLE);

    private final TopicRouteResolver routeResolver;
    private final DeviceEventStageRegistry stageRegistry;
    private final SyncStageRunner syncStageRunner;
    private final AsyncStageRunner asyncStageRunner;
    private final List<DeviceEventInterceptor> interceptors;
    private final List<DeviceEventEnricher> enrichers;
    private final List<DeviceEventCallback> callbacks;
    private final MeterRegistry meterRegistry;
    private final BusStatsService statsService;

    private static String orFallback(String value, String fallback) {
        return StrUtil.nullToDefault(value, fallback);
    }

    // ============================================================
    // 主链路片段
    // ============================================================

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    /**
     * 入站调度入口,由 Kafka consumer / 测试 controller 等调用。
     *
     * @param sourceTopic 来源 topic
     * @param rawSource   原始报文
     * @return outcome 摘要(永不 null)
     */
    public DeviceEventOutcome dispatch(String sourceTopic, Object rawSource) {
        long startNanos = System.nanoTime();

        // ① 路由
        Optional<RouteEntry> routeOpt = routeResolver.resolve(sourceTopic);
        if (routeOpt.isEmpty()) {
            return handleNoRoute(sourceTopic, startNanos);
        }
        RouteEntry route = routeOpt.get();

        // ② before canonicalize
        try {
            invokeInterceptors(it -> it.beforeCanonicalize(sourceTopic, rawSource), "before");
        } catch (DeviceEventDropException dropped) {
            return outcomeDropped(dropped, startNanos);
        }

        // ③ canonicalize
        DeviceProtocolEvent event;
        try {
            event = route.getAdapter().canonicalize(rawSource);
        } catch (Exception e) {
            return handleCanonicalizeFailure(route, sourceTopic, e, startNanos);
        }
        syncContext(event);

        // ④ after canonicalize
        try {
            invokeInterceptors(it -> it.afterCanonicalize(event), "after");
        } catch (DeviceEventDropException dropped) {
            return outcomeDropped(dropped, startNanos);
        }

        // ⑤ enrich
        StageContext context = StageContext.create(event)
            .put(BusConstants.Ctx.SOURCE_TOPIC, sourceTopic)
            .put(BusConstants.Ctx.DISPATCH_GROUP, route.getGroup());
        invokeEnrichers(event, context);
        logStart(event, route.getGroup());

        // ⑥ 三相执行
        return runPipeline(event, context, route.getGroup(), startNanos);
    }

    /**
     * 跑 PRE → (按需) CORE → POST,失败时仍触发 POST 旁路避免故障半径放大。
     */
    private DeviceEventOutcome runPipeline(DeviceProtocolEvent event, StageContext context,
                                           DispatchGroupEnum group, long startNanos) {
        List<StageRecord> records = new ArrayList<>();
        AtomicInteger seq = new AtomicInteger(0);

        if (!runPhase(StagePhaseEnum.PRE, event, context, records, seq)) {
            fireFailureSafePost(event, context);
            return finish(event, records, startNanos, group, PipelineStatusEnum.FAILED, "PRE phase failed");
        }
        if (CORE_GROUPS.contains(group) && !runPhase(StagePhaseEnum.CORE, event, context, records, seq)) {
            fireFailureSafePost(event, context);
            return finish(event, records, startNanos, group, PipelineStatusEnum.FAILED, "CORE phase failed");
        }
        firePost(event, context);
        return finish(event, records, startNanos, group, PipelineStatusEnum.SUCCESS, null);
    }

    /**
     * 同步执行 phase 的 stage 链,空 phase 视为成功。
     */
    private boolean runPhase(StagePhaseEnum phase, DeviceProtocolEvent event, StageContext context,
                             List<StageRecord> records, AtomicInteger seq) {
        List<DeviceEventStage> stages = stageRegistry.byPhase(phase);
        return CollUtil.isEmpty(stages) || syncStageRunner.run(phase, stages, event, context, records, seq);
    }

    // ============================================================
    // Interceptor / Enricher 钩子链
    // ============================================================

    private void firePost(DeviceProtocolEvent event, StageContext context) {
        asyncStageRunner.fireAndForget(stageRegistry.byPhase(StagePhaseEnum.POST), event, context);
    }

    /**
     * PRE/CORE 失败时仍触发 POST(桥接 / 告警 / 指标),失败 swallow。
     */
    private void fireFailureSafePost(DeviceProtocolEvent event, StageContext context) {
        try {
            firePost(event, context);
        } catch (Exception swallow) {
            log.warn("[bus.dispatch] failure-safe POST fire failed traceId={}",
                event.getTraceId(), swallow);
        }
    }

    /**
     * DropException 透传终止 pipeline,其它异常 swallow。
     */
    private void invokeInterceptors(InterceptorAction action, String phase) {
        if (CollUtil.isEmpty(interceptors)) {
            return;
        }
        for (DeviceEventInterceptor it : interceptors) {
            try {
                action.apply(it);
            } catch (DeviceEventDropException drop) {
                throw drop;
            } catch (Exception e) {
                log.warn("[bus.interceptor.{}] {} threw, swallowed",
                    phase, it.getClass().getSimpleName(), e);
            }
        }
    }

    // ============================================================
    // 收尾 + 失败路径
    // ============================================================

    /**
     * 按 getOrder() 升序执行 enricher,单个失败 swallow。
     */
    private void invokeEnrichers(DeviceProtocolEvent event, StageContext context) {
        if (CollUtil.isEmpty(enrichers)) {
            return;
        }
        enrichers.stream()
            .sorted(Comparator.comparingInt(DeviceEventEnricher::getOrder))
            .forEach(e -> {
                try {
                    e.enrich(event, context);
                } catch (Exception ex) {
                    log.warn("[bus.enricher] {} threw, swallowed traceId={}",
                        e.getClass().getSimpleName(), event.getTraceId(), ex);
                }
            });
    }

    /**
     * pipeline 唯一收口:构造 outcome、打日志、上报双指标、触发 callback。
     */
    private DeviceEventOutcome finish(DeviceProtocolEvent event, List<StageRecord> records,
                                      long startNanos, DispatchGroupEnum group,
                                      PipelineStatusEnum status, String failureReason) {
        long ms = (System.nanoTime() - startNanos) / 1_000_000L;
        DeviceEventOutcome outcome = DeviceEventOutcome.builder()
            .status(status).stages(records)
            .totalLatencyMs(ms).failureReason(failureReason)
            .build();

        String protocol = orFallback(event.getProtocolType(), INTERNAL);
        String action = orFallback(event.getEventType(), FALLBACK);

        log.info("{} done traceId={} clientId={} action={} status={} stages={} latency={}ms group={}{}",
            BusConstants.Log.DISPATCH, event.getTraceId(),
            orFallback(event.getClientId(), FALLBACK), action,
            status.getValue(), outcome.effectiveStageCount(), ms, group.getValue(),
            failureReason == null ? StrUtil.EMPTY : (" reason=" + failureReason));

        recordMetrics(protocol, action, group, status, ms);
        invokeCallbacks(event, outcome);
        return outcome;
    }

    /**
     * Micrometer(节点级 P99) + Redis(集群业务报表) 双写。
     */
    private void recordMetrics(String protocol, String action, DispatchGroupEnum group,
                               PipelineStatusEnum status, long ms) {
        Tags tags = Tags.of(
            "protocol", protocol, "action", action,
            "group", group.name(), "status", status.getValue());
        meterRegistry.counter(BusConstants.Metric.DISPATCH_TOTAL, tags).increment();
        meterRegistry.timer(BusConstants.Metric.DISPATCH_LATENCY,
            "protocol", protocol, "group", group.name()).record(ms, TimeUnit.MILLISECONDS);
        statsService.incrementDispatch(protocol, action, group.name(), status.getValue());
    }

    private void invokeCallbacks(DeviceProtocolEvent event, DeviceEventOutcome outcome) {
        if (CollUtil.isEmpty(callbacks)) {
            return;
        }
        for (DeviceEventCallback cb : callbacks) {
            try {
                cb.onComplete(event, outcome);
            } catch (Exception e) {
                log.warn("[bus.callback] {} threw, swallowed",
                    cb.getClass().getSimpleName(), e);
            }
        }
    }

    private DeviceEventOutcome handleNoRoute(String sourceTopic, long startNanos) {
        log.warn("{} topic={}", BusConstants.Log.NO_ROUTE, sourceTopic);
        meterRegistry.counter(BusConstants.Metric.NO_ROUTE,
            "topic", orFallback(sourceTopic, FALLBACK)).increment();
        statsService.incrementNoRoute(sourceTopic);
        statsService.incrementDispatch(UNKNOWN, FALLBACK, FALLBACK, PipelineStatusEnum.NO_ROUTE.getValue());
        return DeviceEventOutcome.builder()
            .status(PipelineStatusEnum.NO_ROUTE)
            .stages(List.of())
            .totalLatencyMs(elapsedMs(startNanos))
            .failureReason("no route for topic=" + sourceTopic)
            .build();
    }

    private DeviceEventOutcome handleCanonicalizeFailure(RouteEntry route, String sourceTopic,
                                                         Exception e, long startNanos) {
        String protocol = route.getAdapter().supports().name();
        log.error("{} canonicalize failed topic={} adapter={} err={}",
            BusConstants.Log.STAGE_FAIL, sourceTopic,
            route.getAdapter().getClass().getSimpleName(), e.getMessage(), e);
        meterRegistry.counter(BusConstants.Metric.CANONICALIZE_FAIL,
            "protocol", protocol,
            "topic", orFallback(sourceTopic, FALLBACK)).increment();
        statsService.incrementCanonicalizeFail(protocol, sourceTopic);
        statsService.incrementDispatch(protocol, FALLBACK, route.getGroup().name(),
            PipelineStatusEnum.FAILED.getValue());
        return DeviceEventOutcome.builder()
            .status(PipelineStatusEnum.FAILED)
            .stages(List.of())
            .totalLatencyMs(elapsedMs(startNanos))
            .failureReason(e.getMessage())
            .build();
    }

    // ============================================================
    // 工具
    // ============================================================

    private DeviceEventOutcome outcomeDropped(DeviceEventDropException drop, long startNanos) {
        log.info("{} reason={}", BusConstants.Log.DROP, drop.getReason());
        statsService.incrementDispatch(UNKNOWN, FALLBACK, FALLBACK, PipelineStatusEnum.DROPPED.getValue());
        return DeviceEventOutcome.builder()
            .status(PipelineStatusEnum.DROPPED)
            .stages(List.of())
            .totalLatencyMs(elapsedMs(startNanos))
            .failureReason(drop.getReason())
            .build();
    }

    /**
     * dispatcher 入口日志(关键位置,1 行 INFO 含 traceId / protocol / action / topic / clientId / group)。
     */
    private void logStart(DeviceProtocolEvent event, DispatchGroupEnum group) {
        log.info("{} start traceId={} protocol={} action={} topic={} clientId={} group={}",
            BusConstants.Log.DISPATCH, event.getTraceId(),
            orFallback(event.getProtocolType(), INTERNAL),
            orFallback(event.getEventType(), FALLBACK),
            orFallback(event.getTopic(), FALLBACK),
            orFallback(event.getClientId(), FALLBACK),
            group.getValue());
    }

    /**
     * canonicalize 后同步 ContextUtil。仅当当前为空时设,避免覆盖外层(Kafka consumer / web 拦截器)已设的值。
     * <p>{@link ContextUtil#setTenantId} 内部联动设置 tenantBasePoolName + tenantExtendPoolName,无需再单独调。
     */
    private void syncContext(DeviceProtocolEvent event) {
        Optional.of(event.getTraceId())
            .filter(StrUtil::isNotBlank)
            .filter(t -> StrUtil.isBlank(ContextUtil.getLogTraceId()))
            .ifPresent(ContextUtil::setLogTraceId);

        Optional.ofNullable(event.getTenantId())
            .filter(StrUtil::isNotBlank)
            .filter(t -> ContextUtil.isEmptyTenantId())
            .ifPresent(ContextUtil::setTenantId);
    }

    @FunctionalInterface
    private interface InterceptorAction {
        void apply(DeviceEventInterceptor interceptor);
    }
}
