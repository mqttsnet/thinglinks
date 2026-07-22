package com.mqttsnet.thinglinks.mqs.bus.dispatcher;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.thinglinks.bus.hook.StageFailureHandler;
import com.mqttsnet.thinglinks.bus.hook.StageGuard;
import com.mqttsnet.thinglinks.bus.stage.DeviceEventStage;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.constants.bus.BusConstants;
import com.mqttsnet.thinglinks.dto.bus.StageRecord;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import com.mqttsnet.thinglinks.enumeration.bus.StageStatusEnum;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 同步阶段执行器,PRE / CORE 阶段用,任一 stage 失败立即终止 phase。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SyncStageRunner {

    private static final String METRIC_STATUS_OK = "ok";
    private static final String METRIC_STATUS_FAIL = "fail";
    private static final String SKIP_NOT_SUPPORTS = "supports=false";
    private static final String SKIP_GUARD = "guard=false";

    private final List<StageGuard> guards;
    private final List<StageFailureHandler> failureHandlers;
    private final MeterRegistry meterRegistry;

    private static long elapsedMs(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000L;
    }

    private static StageRecord record(DeviceEventStage s, StagePhaseEnum p, int seq,
                                      StageStatusEnum status, long ms, String err, String skip) {
        return StageRecord.builder()
            .stageName(s.getName()).phase(p).sequence(seq).status(status)
            .latencyMs(ms).errorMsg(err).skipReason(skip).build();
    }

    /**
     * 顺序执行 phase 的 stage 链,任一抛异常终止 phase 并触发失败钩子。
     */
    public boolean run(StagePhaseEnum phase, List<DeviceEventStage> stages,
                       DeviceProtocolEvent event, StageContext context,
                       List<StageRecord> records, AtomicInteger counter) {
        if (CollUtil.isEmpty(stages)) {
            return true;
        }
        for (DeviceEventStage stage : stages) {
            int seq = counter.incrementAndGet();
            Optional<String> skip = skipReason(stage, event);
            if (skip.isPresent()) {
                records.add(record(stage, phase, seq, StageStatusEnum.SKIPPED, 0L, null, skip.get()));
                continue;
            }
            if (!executeOne(stage, phase, seq, event, context, records)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 返回跳过原因(supports / guard);present=跳过。
     */
    private Optional<String> skipReason(DeviceEventStage stage, DeviceProtocolEvent event) {
        if (!stage.supports(event)) {
            return Optional.of(SKIP_NOT_SUPPORTS);
        }
        boolean allPass = guards.stream().allMatch(g -> g.shouldExecute(stage, event));
        return allPass ? Optional.empty() : Optional.of(SKIP_GUARD);
    }

    /**
     * 执行单 stage,返回是否成功。
     */
    private boolean executeOne(DeviceEventStage stage, StagePhaseEnum phase, int seq,
                               DeviceProtocolEvent event, StageContext context,
                               List<StageRecord> records) {
        long start = System.nanoTime();
        try {
            stage.execute(event, context);
            long ms = elapsedMs(start);
            records.add(record(stage, phase, seq, StageStatusEnum.SUCCESS, ms, null, null));
            recordMetric(stage, phase, METRIC_STATUS_OK, ms);
            return true;
        } catch (Exception e) {
            long ms = elapsedMs(start);
            records.add(record(stage, phase, seq, StageStatusEnum.FAILED, ms, e.getMessage(), null));
            recordMetric(stage, phase, METRIC_STATUS_FAIL, ms);
            log.warn("{} stage={} phase={} traceId={} clientId={} action={} err={}",
                BusConstants.Log.STAGE_FAIL, stage.getName(), phase, event.getTraceId(),
                event.getClientId(), event.getEventType(), e.getMessage());
            fireFailure(stage, event, e);
            return false;
        }
    }

    /**
     * 触发失败处理链;handler 自身异常 swallow。
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
     * 上报 Micrometer counter + timer。
     */
    private void recordMetric(DeviceEventStage stage, StagePhaseEnum phase, String status, long ms) {
        Tags tags = Tags.of("stage", stage.getName(), "phase", phase.getValue(), "status", status);
        meterRegistry.counter(BusConstants.Metric.STAGE_EXECUTIONS, tags).increment();
        meterRegistry.timer(BusConstants.Metric.STAGE_LATENCY, tags).record(ms, TimeUnit.MILLISECONDS);
    }
}
