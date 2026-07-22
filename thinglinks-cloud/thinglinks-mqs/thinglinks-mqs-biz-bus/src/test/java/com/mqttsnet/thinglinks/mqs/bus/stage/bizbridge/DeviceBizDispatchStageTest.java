package com.mqttsnet.thinglinks.mqs.bus.stage.bizbridge;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.mqs.event.assembler.CommonDeviceEventAssembler;
import com.mqttsnet.thinglinks.mqs.event.counter.LinkDataReportCounter;
import com.mqttsnet.thinglinks.mqs.event.dispatcher.DeviceEventDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS 设备业务分发阶段")
class DeviceBizDispatchStageTest {

    private final CommonDeviceEventAssembler assembler = org.mockito.Mockito.mock(CommonDeviceEventAssembler.class);
    private final DeviceEventDispatcher dispatcher = org.mockito.Mockito.mock(DeviceEventDispatcher.class);
    private final LinkDataReportCounter counter = org.mockito.Mockito.mock(LinkDataReportCounter.class);
    private final DeviceBizDispatchStage stage = new DeviceBizDispatchStage(assembler, dispatcher, counter);

    @Test
    @DisplayName("验证 PUBLISH 事件大小写归一化后分发业务事件并累计上行计数")
    void executeDispatchesDomainEventAndCountsPublishCaseInsensitively() {
        DeviceProtocolEvent protocolEvent = DeviceProtocolEvent.builder()
            .eventType("publish")
            .clientId("client-1")
            .build();
        CommonDeviceEvent domainEvent = CommonDeviceEvent.builder()
            .actionType(DeviceActionTypeEnum.PUBLISH)
            .clientId("client-1")
            .build();
        when(assembler.assemble(protocolEvent)).thenReturn(Optional.of(domainEvent));

        stage.execute(protocolEvent, StageContext.create(protocolEvent));

        verify(dispatcher).dispatch(domainEvent);
        verify(counter).incrementUpLink();
    }

    @Test
    @DisplayName("验证协议事件无法组装成业务事件时跳过分发和计数")
    void executeSkipsWhenAssemblerCannotBuildDomainEvent() {
        DeviceProtocolEvent protocolEvent = DeviceProtocolEvent.builder()
            .eventType("CONNECT")
            .clientId("client-1")
            .build();
        when(assembler.assemble(protocolEvent)).thenReturn(Optional.empty());

        stage.execute(protocolEvent, StageContext.create(protocolEvent));

        verify(dispatcher, never()).dispatch(org.mockito.ArgumentMatchers.any());
        verify(counter, never()).incrementUpLink();
    }

    @Test
    @DisplayName("验证阶段只支持具备非空动作类型的协议事件")
    void supportsRequiresNonBlankEventType() {
        org.assertj.core.api.Assertions.assertThat(stage.supports(DeviceProtocolEvent.builder()
            .eventType("PING")
            .build())).isTrue();
        org.assertj.core.api.Assertions.assertThat(stage.supports(DeviceProtocolEvent.builder()
            .eventType(" ")
            .build())).isFalse();
        org.assertj.core.api.Assertions.assertThat(stage.supports(null)).isFalse();
    }
}
