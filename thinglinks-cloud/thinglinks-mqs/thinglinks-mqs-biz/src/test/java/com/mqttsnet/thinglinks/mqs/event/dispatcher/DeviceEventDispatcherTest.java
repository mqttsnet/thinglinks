package com.mqttsnet.thinglinks.mqs.event.dispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.mqs.event.hook.DeviceEventContext;
import com.mqttsnet.thinglinks.mqs.event.hook.DeviceEventHook;
import com.mqttsnet.thinglinks.mqs.event.processor.DeviceEventProcessor;
import com.mqttsnet.thinglinks.mqs.service.DeviceEventActionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS 设备业务事件分发器")
class DeviceEventDispatcherTest {

    private final DeviceEventActionService actionService = mock(DeviceEventActionService.class);

    @Test
    @DisplayName("验证 PUBLISH 数据上报只走处理器链路，不写设备动作记录")
    void dispatchRoutesPublishWithoutPersistingDeviceAction() {
        List<String> calls = new ArrayList<>();
        RecordingHook hook = new RecordingHook(100, DeviceActionTypeEnum.PUBLISH, calls);
        RecordingProcessor processor = new RecordingProcessor(DeviceActionTypeEnum.PUBLISH, calls);
        DeviceEventDispatcher dispatcher = new DeviceEventDispatcher(
            List.of(hook), List.of(processor), actionService);

        CommonDeviceEvent event = event(DeviceActionTypeEnum.PUBLISH);

        dispatcher.dispatch(event);

        assertThat(calls).containsExactly("hook.before:PUBLISH", "processor:PUBLISH", "hook.after:PUBLISH");
        verify(actionService, never()).save(event);
    }

    @Test
    @DisplayName("验证连接类生命周期事件即使命不中处理器也会落设备动作记录")
    void dispatchPersistsLifecycleEventEvenWhenNoProcessorMatches() {
        List<String> calls = new ArrayList<>();
        RecordingHook hook = new RecordingHook(100, DeviceActionTypeEnum.CONNECT, calls);
        DeviceEventProcessor publishOnly = new RecordingProcessor(DeviceActionTypeEnum.PUBLISH, calls);
        DeviceEventDispatcher dispatcher = new DeviceEventDispatcher(
            List.of(hook), List.of(publishOnly), actionService);

        CommonDeviceEvent event = event(DeviceActionTypeEnum.CONNECT);

        dispatcher.dispatch(event);

        assertThat(calls).containsExactly("hook.before:CONNECT", "hook.after:CONNECT");
        verify(actionService).save(event);
    }

    @Test
    @DisplayName("验证 Hook 异常被隔离，主处理器和其它 Hook 仍继续执行")
    void dispatchIsolatesHookFailureAndStillRunsMainPathAndOtherHooks() {
        List<String> calls = new ArrayList<>();
        DeviceEventHook failing = new FailingHook(calls);
        RecordingHook healthy = new RecordingHook(200, DeviceActionTypeEnum.PING, calls);
        RecordingProcessor processor = new RecordingProcessor(DeviceActionTypeEnum.PING, calls);
        DeviceEventDispatcher dispatcher = new DeviceEventDispatcher(
            List.of(failing, healthy), List.of(processor), actionService);

        CommonDeviceEvent event = event(DeviceActionTypeEnum.PING);

        dispatcher.dispatch(event);

        assertThat(calls).containsExactly(
            "failing.before",
            "failing.onError:boom",
            "hook.before:PING",
            "processor:PING",
            "hook.after:PING");
        verify(actionService).save(event);
    }

    @Test
    @DisplayName("验证初始化会按 Hook order 排序，保证前后置处理顺序稳定")
    void initSortsHooksByOrder() {
        List<String> calls = new ArrayList<>();
        List<DeviceEventHook> hooks = new ArrayList<>();
        hooks.add(new RecordingHook(200, DeviceActionTypeEnum.CONNECT, calls, "late"));
        hooks.add(new RecordingHook(100, DeviceActionTypeEnum.CONNECT, calls, "early"));
        DeviceEventDispatcher dispatcher = new DeviceEventDispatcher(hooks, List.of(), actionService);

        dispatcher.init();
        dispatcher.dispatch(event(DeviceActionTypeEnum.CONNECT));

        assertThat(calls).containsExactly("early.before:CONNECT", "late.before:CONNECT",
            "early.after:CONNECT", "late.after:CONNECT");
    }

    @Test
    @DisplayName("验证空事件或缺少动作类型时直接跳过，避免错误落库")
    void dispatchSkipsNullEventOrMissingActionType() {
        DeviceEventDispatcher dispatcher = new DeviceEventDispatcher(List.of(), List.of(), actionService);

        dispatcher.dispatch(null);
        dispatcher.dispatch(CommonDeviceEvent.builder().clientId("client-1").build());

        verify(actionService, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private static CommonDeviceEvent event(DeviceActionTypeEnum actionType) {
        return CommonDeviceEvent.builder()
            .actionType(actionType)
            .clientId("client-1")
            .deviceIdentification("device-1")
            .tenantId("1")
            .rawMessage("{\"clientId\":\"client-1\"}")
            .build();
    }

    private static final class RecordingHook implements DeviceEventHook {
        private final int order;
        private final DeviceActionTypeEnum type;
        private final List<String> calls;
        private final String name;

        private RecordingHook(int order, DeviceActionTypeEnum type, List<String> calls) {
            this(order, type, calls, "hook");
        }

        private RecordingHook(int order, DeviceActionTypeEnum type, List<String> calls, String name) {
            this.order = order;
            this.type = type;
            this.calls = calls;
            this.name = name;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public boolean supports(DeviceActionTypeEnum type) {
            return this.type == type;
        }

        @Override
        public void beforeDispatch(DeviceEventContext ctx) {
            calls.add(name + ".before:" + ctx.getType().getValue());
        }

        @Override
        public void afterDispatch(DeviceEventContext ctx) {
            calls.add(name + ".after:" + ctx.getType().getValue());
        }
    }

    private static final class FailingHook implements DeviceEventHook {
        private final List<String> calls;

        private FailingHook(List<String> calls) {
            this.calls = calls;
        }

        @Override
        public boolean supports(DeviceActionTypeEnum type) {
            return DeviceActionTypeEnum.PING == type;
        }

        @Override
        public void beforeDispatch(DeviceEventContext ctx) {
            calls.add("failing.before");
            throw new IllegalStateException("boom");
        }

        @Override
        public void onError(DeviceEventContext ctx, Throwable t) {
            calls.add("failing.onError:" + t.getMessage());
        }
    }

    private static final class RecordingProcessor implements DeviceEventProcessor {
        private final DeviceActionTypeEnum type;
        private final List<String> calls;

        private RecordingProcessor(DeviceActionTypeEnum type, List<String> calls) {
            this.type = type;
            this.calls = calls;
        }

        @Override
        public boolean supports(DeviceActionTypeEnum type) {
            return this.type == type;
        }

        @Override
        public void process(CommonDeviceEvent event) {
            calls.add("processor:" + event.getActionType().getValue());
        }
    }
}
