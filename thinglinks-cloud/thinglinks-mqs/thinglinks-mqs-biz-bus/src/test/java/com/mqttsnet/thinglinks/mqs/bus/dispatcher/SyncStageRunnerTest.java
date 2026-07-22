package com.mqttsnet.thinglinks.mqs.bus.dispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mqttsnet.thinglinks.bus.hook.StageFailureHandler;
import com.mqttsnet.thinglinks.bus.hook.StageGuard;
import com.mqttsnet.thinglinks.bus.stage.DeviceEventStage;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.dto.bus.StageRecord;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import com.mqttsnet.thinglinks.enumeration.bus.StageStatusEnum;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS Bus 同步阶段执行器")
class SyncStageRunnerTest {

    @Test
    @DisplayName("验证成功、supports 跳过、guard 跳过和失败中断都会写入阶段记录")
    void runRecordsSuccessSkipAndFailureAndStopsOnFailure() {
        StageFailureHandler failureHandler = org.mockito.Mockito.mock(StageFailureHandler.class);
        org.mockito.Mockito.when(failureHandler.supports(org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any())).thenReturn(true);
        StageGuard guard = (stage, event) -> !"guarded".equals(stage.getName());
        SyncStageRunner runner = new SyncStageRunner(List.of(guard), List.of(failureHandler),
            new SimpleMeterRegistry());
        List<String> calls = new ArrayList<>();
        List<StageRecord> records = new ArrayList<>();
        DeviceProtocolEvent event = DeviceProtocolEvent.builder()
            .eventType("PUBLISH")
            .traceId("trace-1")
            .clientId("client-1")
            .build();

        boolean ok = runner.run(StagePhaseEnum.PRE, List.of(
                stage("success", true, false, calls),
                stage("unsupported", false, false, calls),
                stage("guarded", true, false, calls),
                stage("failed", true, true, calls),
                stage("afterFailed", true, false, calls)),
            event, StageContext.create(event), records, new AtomicInteger());

        assertThat(ok).isFalse();
        assertThat(calls).containsExactly("success", "failed");
        assertThat(records).extracting(StageRecord::getStageName)
            .containsExactly("success", "unsupported", "guarded", "failed");
        assertThat(records).extracting(StageRecord::getStatus)
            .containsExactly(StageStatusEnum.SUCCESS, StageStatusEnum.SKIPPED,
                StageStatusEnum.SKIPPED, StageStatusEnum.FAILED);
        assertThat(records.get(1).getSkipReason()).isEqualTo("supports=false");
        assertThat(records.get(2).getSkipReason()).isEqualTo("guard=false");
        verify(failureHandler).onFailure(
            org.mockito.ArgumentMatchers.argThat(stage -> stage != null && "failed".equals(stage.getName())),
            org.mockito.ArgumentMatchers.eq(event),
            org.mockito.ArgumentMatchers.any());
    }

    private static DeviceEventStage stage(String name, boolean supports, boolean fails, List<String> calls) {
        return new DeviceEventStage() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public StagePhaseEnum getPhase() {
                return StagePhaseEnum.PRE;
            }

            @Override
            public int getOrder() {
                return 0;
            }

            @Override
            public boolean supports(DeviceProtocolEvent event) {
                return supports;
            }

            @Override
            public void execute(DeviceProtocolEvent event, StageContext context) {
                calls.add(name);
                if (fails) {
                    throw new IllegalStateException("failed");
                }
            }
        };
    }
}
