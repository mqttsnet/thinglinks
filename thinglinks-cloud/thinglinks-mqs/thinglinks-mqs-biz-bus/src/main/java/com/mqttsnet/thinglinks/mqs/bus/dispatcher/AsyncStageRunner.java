package com.mqttsnet.thinglinks.mqs.bus.dispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.bus.hook.StageFailureHandler;
import com.mqttsnet.thinglinks.bus.hook.StageGuard;
import com.mqttsnet.thinglinks.bus.stage.DeviceEventStage;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.constants.bus.BusConstants;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 异步阶段执行器,POST 阶段用,每 stage 提交专属池,失败隔离。
 * 池命名:{@code mqsBus<StageBaseName>Executor},缺省走 {@link BusConstants.Pool#DEFAULT_POST}。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AsyncStageRunner {

    private static final String POOL_PREFIX = "mqsBus";
    private static final String POOL_SUFFIX = "Executor";
    private static final String STAGE_SUFFIX = "Stage";

    private static final String METRIC_STATUS_OK = "ok";
    private static final String METRIC_STATUS_FAIL = "fail";

    private final ApplicationContext applicationContext;
    private final List<StageGuard> guards;
    private final List<StageFailureHandler> failureHandlers;
    private final MeterRegistry meterRegistry;

    private static String stripStageSuffix(String name) {
        if (StrUtil.isBlank(name)) {
            return StrUtil.EMPTY;
        }
        return name.endsWith(STAGE_SUFFIX) ? name.substring(0, name.length() - STAGE_SUFFIX.length()) : name;
    }

    private static long elapsedMs(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000L;
    }

    /**
     * 启动期 fail-fast,默认 POST 池缺失则拒启动。
     */
    @PostConstruct
    public void verifyDefaultPool() {
        findPool(BusConstants.Pool.DEFAULT_POST).orElseThrow(() -> {
            String msg = "[bus.async] CRITICAL: default POST pool '" + BusConstants.Pool.DEFAULT_POST
                + "' missing! POST stages will stall Kafka consumer. Configure dynamictp.yml.";
            log.error(msg);
            return new IllegalStateException(msg);
        });
        log.info("[bus.async] default POST pool '{}' verified ready", BusConstants.Pool.DEFAULT_POST);
    }

    /**
     * 提交 POST stage 列表,fire-and-forget。worker 内手动恢复 ContextUtil 防 ttl 漏配。
     *
     * @param stages  POST 阶段列表
     * @param event   设备协议事件
     * @param context 阶段上下文
     */
    public void fireAndForget(List<DeviceEventStage> stages,
                              DeviceProtocolEvent event, StageContext context) {
        if (CollUtil.isEmpty(stages)) {
            return;
        }
        Map<String, String> snapshot = snapshotLocalMap();
        stages.stream()
            .filter(s -> s.supports(event))
            .filter(s -> guards.stream().allMatch(g -> g.shouldExecute(s, event)))
            .forEach(s -> submit(s, event, context, snapshot));
    }

    /**
     * 提交单 stage,池缺失走 inline 兜底。
     *
     * @param stage    待执行阶段
     * @param event    设备协议事件
     * @param context  阶段上下文
     * @param snapshot ContextUtil LocalMap 快照
     */
    private void submit(DeviceEventStage stage, DeviceProtocolEvent event,
                        StageContext context, Map<String, String> snapshot) {
        Optional<ThreadPoolExecutor> pool = resolvePool(stage);
        log.debug("{} stage={} pool={} traceId={}", BusConstants.Log.POST_SUBMIT,
            stage.getName(), pool.map(Object::toString).orElse("FALLBACK_INLINE"), event.getTraceId());
        Runnable task = () -> runWithContext(snapshot, stage, event, context);
        pool.ifPresentOrElse(p -> p.execute(task), () -> runOne(stage, event, context));
    }

    /**
     * 浅拷贝 ContextUtil LocalMap;空时返 null。
     *
     * @return LocalMap 浅拷贝;空时返 null
     */
    private Map<String, String> snapshotLocalMap() {
        return Optional.of(ContextUtil.getLocalMap())
            .filter(m -> !m.isEmpty())
            .map(HashMap::new)
            .orElse(null);
    }

    /**
     * worker 入口:恢复 ContextUtil → 执行 → finally remove。
     *
     * @param snapshot ContextUtil LocalMap 快照
     * @param stage    待执行阶段
     * @param event    设备协议事件
     * @param context  阶段上下文
     */
    private void runWithContext(Map<String, String> snapshot, DeviceEventStage stage,
                                DeviceProtocolEvent event, StageContext context) {
        boolean restored = false;
        if (snapshot != null) {
            ContextUtil.setLocalMap(snapshot);
            restored = true;
        }
        if (event != null && StrUtil.isNotBlank(event.getTraceId())) {
            ContextUtil.setLogTraceId(event.getTraceId());
            restored = true;
        }
        // 事件自带 tenantId 是租户的权威来源:snapshot 可能因上游未解析而缺租户段,
        // 此处兜底回填,保证 POST 异步阶段(桥接 / 告警 relay)统计 key 不丢租户。
        if (event != null && StrUtil.isNotBlank(event.getTenantId()) && ContextUtil.isEmptyTenantId()) {
            ContextUtil.setTenantId(event.getTenantId());
            restored = true;
        }
        try {
            runOne(stage, event, context);
        } finally {
            if (restored) {
                ContextUtil.remove();
            }
        }
    }

    /**
     * 单 stage 执行 + 指标 + 失败钩子。
     *
     * @param stage   待执行阶段
     * @param event   设备协议事件
     * @param context 阶段上下文
     */
    private void runOne(DeviceEventStage stage, DeviceProtocolEvent event, StageContext context) {
        long start = System.nanoTime();
        try {
            stage.execute(event, context);
            recordMetric(stage, METRIC_STATUS_OK, elapsedMs(start));
        } catch (Exception e) {
            recordMetric(stage, METRIC_STATUS_FAIL, elapsedMs(start));
            log.warn("{} stage={} phase=POST traceId={} clientId={} action={} err={}",
                BusConstants.Log.STAGE_FAIL, stage.getName(), event.getTraceId(),
                event.getClientId(), event.getEventType(), e.getMessage());
            fireFailure(stage, event, e);
        }
    }

    /**
     * 触发失败钩子;handler 自身异常 swallow。
     *
     * @param stage 失败阶段
     * @param event 设备协议事件
     * @param error 失败异常
     */
    private void fireFailure(DeviceEventStage stage, DeviceProtocolEvent event, Throwable error) {
        failureHandlers.stream()
            .filter(h -> h.supports(stage, error))
            .forEach(h -> {
                try {
                    h.onFailure(stage, event, error);
                } catch (Exception swallow) {
                    log.warn("[bus.failure-handler] handler {} threw, swallowed",
                        h.getClass().getSimpleName(), swallow);
                }
            });
    }

    /**
     * 优先专属池(mqsBus<Base>Executor),fallback 默认 POST 池。
     *
     * @param stage 待执行阶段
     * @return 命中的线程池;均无返 {@link Optional#empty()}
     */
    private Optional<ThreadPoolExecutor> resolvePool(DeviceEventStage stage) {
        String specific = POOL_PREFIX + stripStageSuffix(stage.getName()) + POOL_SUFFIX;
        return findPool(specific).or(() -> findPool(BusConstants.Pool.DEFAULT_POST));
    }

    private Optional<ThreadPoolExecutor> findPool(String name) {
        if (!applicationContext.containsBean(name)) {
            return Optional.empty();
        }
        try {
            return Optional.of(applicationContext.getBean(name, ThreadPoolExecutor.class));
        } catch (Exception ignore) {
            return Optional.empty();
        }
    }

    private void recordMetric(DeviceEventStage stage, String status, long ms) {
        Tags tags = Tags.of("stage", stage.getName(), "phase", StagePhaseEnum.POST.getValue(), "status", status);
        meterRegistry.counter(BusConstants.Metric.STAGE_EXECUTIONS, tags).increment();
        meterRegistry.timer(BusConstants.Metric.STAGE_LATENCY, tags).record(ms, TimeUnit.MILLISECONDS);
    }
}
