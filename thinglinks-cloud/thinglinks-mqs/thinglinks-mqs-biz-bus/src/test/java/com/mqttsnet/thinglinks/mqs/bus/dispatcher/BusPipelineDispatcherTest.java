package com.mqttsnet.thinglinks.mqs.bus.dispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.bus.adapter.ProtocolEdgeAdapter;
import com.mqttsnet.thinglinks.bus.hook.DeviceEventCallback;
import com.mqttsnet.thinglinks.bus.hook.DeviceEventDropException;
import com.mqttsnet.thinglinks.bus.hook.DeviceEventInterceptor;
import com.mqttsnet.thinglinks.bus.route.RouteEntry;
import com.mqttsnet.thinglinks.bus.stage.DeviceEventStage;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.dto.bus.DeviceEventOutcome;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import com.mqttsnet.thinglinks.enumeration.bus.PipelineStatusEnum;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import com.mqttsnet.thinglinks.enumeration.bus.StageStatusEnum;
import com.mqttsnet.thinglinks.mqs.bus.core.route.TopicRouteResolver;
import com.mqttsnet.thinglinks.mqs.bus.core.stage.DeviceEventStageRegistry;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS Bus 管道调度器")
class BusPipelineDispatcherTest {

    private final TopicRouteResolver routeResolver = org.mockito.Mockito.mock(TopicRouteResolver.class);
    private final AsyncStageRunner asyncStageRunner = org.mockito.Mockito.mock(AsyncStageRunner.class);
    private final BusStatsService statsService = org.mockito.Mockito.mock(BusStatsService.class);
    private final DeviceEventCallback callback = org.mockito.Mockito.mock(DeviceEventCallback.class);

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    @DisplayName("验证设备数据路由按 PRE/CORE 同步执行，并将 POST 阶段异步提交")
    void dispatchRunsPreCoreAndSubmitsPostForDeviceDataRoute() {
        List<String> calls = new ArrayList<>();
        DeviceProtocolEvent event = event(DeviceActionTypeEnum.PUBLISH.getValue());
        BusPipelineDispatcher dispatcher = dispatcher(List.of(
            stage("pre", StagePhaseEnum.PRE, calls, false),
            stage("core", StagePhaseEnum.CORE, calls, false),
            stage("post", StagePhaseEnum.POST, calls, false)), List.of(), event);

        DeviceEventOutcome outcome = dispatcher.dispatch("source.topic", "{}");

        assertThat(outcome.getStatus()).isEqualTo(PipelineStatusEnum.SUCCESS);
        assertThat(calls).containsExactly("pre", "core");
        assertThat(outcome.getStages()).hasSize(2);
        assertThat(outcome.getStages()).extracting(s -> s.getStatus())
            .containsExactly(StageStatusEnum.SUCCESS, StageStatusEnum.SUCCESS);
        verify(asyncStageRunner).fireAndForget(
            org.mockito.ArgumentMatchers.argThat(stages -> stages != null && stages.size() == 1
                && "post".equals(stages.get(0).getName())),
            org.mockito.ArgumentMatchers.eq(event),
            org.mockito.ArgumentMatchers.any(StageContext.class));
        verify(callback).onComplete(org.mockito.ArgumentMatchers.eq(event),
            org.mockito.ArgumentMatchers.eq(outcome));
    }

    @Test
    @DisplayName("验证 topic 无路由时返回 NO_ROUTE 且不进入后续阶段")
    void dispatchReturnsNoRouteWhenTopicCannotResolve() {
        when(routeResolver.resolve("missing.topic")).thenReturn(Optional.empty());
        BusPipelineDispatcher dispatcher = newDispatcher(new DeviceEventStageRegistry(List.of()), List.of());

        DeviceEventOutcome outcome = dispatcher.dispatch("missing.topic", "{}");

        assertThat(outcome.getStatus()).isEqualTo(PipelineStatusEnum.NO_ROUTE);
        assertThat(outcome.getFailureReason()).contains("missing.topic");
        verify(statsService).incrementNoRoute("missing.topic");
        verify(asyncStageRunner, never()).fireAndForget(org.mockito.ArgumentMatchers.anyList(),
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(callback, never()).onComplete(org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("验证拦截器在协议归一化前丢弃事件时不会调用适配器")
    void dispatchDropsWhenInterceptorRejectsBeforeCanonicalize() {
        ProtocolEdgeAdapter adapter = org.mockito.Mockito.mock(ProtocolEdgeAdapter.class);
        when(adapter.supports()).thenReturn(ProtocolTypeEnum.MQTT);
        when(routeResolver.resolve("source.topic")).thenReturn(Optional.of(RouteEntry.of(
            "source.topic", MatchModeEnum.EXACT, DispatchGroupEnum.DEVICE_DATA, adapter, 0)));
        DeviceEventInterceptor dropper = new DeviceEventInterceptor() {
            @Override
            public void beforeCanonicalize(String sourceTopic, Object rawSource) {
                throw new DeviceEventDropException("blocked");
            }
        };
        BusPipelineDispatcher dispatcher = newDispatcher(new DeviceEventStageRegistry(List.of()),
            List.of(dropper));

        DeviceEventOutcome outcome = dispatcher.dispatch("source.topic", "{}");

        assertThat(outcome.getStatus()).isEqualTo(PipelineStatusEnum.DROPPED);
        assertThat(outcome.getFailureReason()).isEqualTo("blocked");
        verify(adapter, never()).canonicalize(org.mockito.ArgumentMatchers.any());
        verify(asyncStageRunner, never()).fireAndForget(org.mockito.ArgumentMatchers.anyList(),
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("验证协议适配失败时记录归一化失败并返回 FAILED")
    void dispatchReturnsFailedWhenAdapterCannotCanonicalize() {
        ProtocolEdgeAdapter adapter = org.mockito.Mockito.mock(ProtocolEdgeAdapter.class);
        when(adapter.supports()).thenReturn(ProtocolTypeEnum.MQTT);
        when(adapter.canonicalize("{}")).thenThrow(new IllegalArgumentException("bad payload"));
        when(routeResolver.resolve("source.topic")).thenReturn(Optional.of(RouteEntry.of(
            "source.topic", MatchModeEnum.EXACT, DispatchGroupEnum.DEVICE_DATA, adapter, 0)));
        BusPipelineDispatcher dispatcher = newDispatcher(new DeviceEventStageRegistry(List.of()), List.of());

        DeviceEventOutcome outcome = dispatcher.dispatch("source.topic", "{}");

        assertThat(outcome.getStatus()).isEqualTo(PipelineStatusEnum.FAILED);
        assertThat(outcome.getFailureReason()).isEqualTo("bad payload");
        verify(statsService).incrementCanonicalizeFail(ProtocolTypeEnum.MQTT.name(), "source.topic");
        verify(asyncStageRunner, never()).fireAndForget(org.mockito.ArgumentMatchers.anyList(),
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("验证 CORE 阶段失败后仍异步触发 POST 阶段用于补偿/转发")
    void dispatchFiresPostEvenWhenCoreStageFails() {
        List<String> calls = new ArrayList<>();
        DeviceProtocolEvent event = event(DeviceActionTypeEnum.PUBLISH.getValue());
        BusPipelineDispatcher dispatcher = dispatcher(List.of(
            stage("pre", StagePhaseEnum.PRE, calls, false),
            stage("core", StagePhaseEnum.CORE, calls, true),
            stage("post", StagePhaseEnum.POST, calls, false)), List.of(), event);

        DeviceEventOutcome outcome = dispatcher.dispatch("source.topic", "{}");

        assertThat(outcome.getStatus()).isEqualTo(PipelineStatusEnum.FAILED);
        assertThat(outcome.firstFailedStage()).contains("core");
        assertThat(calls).containsExactly("pre", "core");
        verify(asyncStageRunner).fireAndForget(
            org.mockito.ArgumentMatchers.argThat(stages -> stages != null && stages.size() == 1
                && "post".equals(stages.get(0).getName())),
            org.mockito.ArgumentMatchers.eq(event),
            org.mockito.ArgumentMatchers.any(StageContext.class));
    }

    private BusPipelineDispatcher dispatcher(List<DeviceEventStage> stages,
                                             List<DeviceEventInterceptor> interceptors,
                                             DeviceProtocolEvent event) {
        ProtocolEdgeAdapter adapter = org.mockito.Mockito.mock(ProtocolEdgeAdapter.class);
        when(adapter.supports()).thenReturn(ProtocolTypeEnum.MQTT);
        when(adapter.canonicalize("{}")).thenReturn(event);
        when(routeResolver.resolve("source.topic")).thenReturn(Optional.of(RouteEntry.of(
            "source.topic", MatchModeEnum.EXACT, DispatchGroupEnum.DEVICE_DATA, adapter, 0)));
        return newDispatcher(new DeviceEventStageRegistry(stages), interceptors);
    }

    private BusPipelineDispatcher newDispatcher(DeviceEventStageRegistry stageRegistry,
                                                List<DeviceEventInterceptor> interceptors) {
        SyncStageRunner syncStageRunner = new SyncStageRunner(List.of(), List.of(),
            new SimpleMeterRegistry());
        return new BusPipelineDispatcher(
            routeResolver,
            stageRegistry,
            syncStageRunner,
            asyncStageRunner,
            interceptors,
            List.of(),
            List.of(callback),
            new SimpleMeterRegistry(),
            statsService);
    }

    private static DeviceProtocolEvent event(String actionType) {
        return DeviceProtocolEvent.builder()
            .protocolType(ProtocolTypeEnum.MQTT.getValue())
            .eventType(actionType)
            .tenantId("1")
            .traceId("trace-1")
            .clientId("client-1")
            .build();
    }

    private static DeviceEventStage stage(String name, StagePhaseEnum phase, List<String> calls, boolean fails) {
        return new DeviceEventStage() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public StagePhaseEnum getPhase() {
                return phase;
            }

            @Override
            public int getOrder() {
                return 0;
            }

            @Override
            public void execute(DeviceProtocolEvent event, StageContext context) {
                calls.add(name);
                if (fails) {
                    throw new IllegalStateException("stage failed");
                }
            }
        };
    }
}
