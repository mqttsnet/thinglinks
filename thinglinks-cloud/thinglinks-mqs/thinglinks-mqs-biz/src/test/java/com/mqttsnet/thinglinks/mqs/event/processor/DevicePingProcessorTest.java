package com.mqttsnet.thinglinks.mqs.event.processor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.constants.bus.BusKafkaJsonField;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenInnerFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MQS 设备心跳处理器")
class DevicePingProcessorTest {

    @Mock
    private DeviceOpenInnerFacade deviceOpenInnerFacade;
    @Mock
    private CachePlusOps cachePlusOps;

    @Test
    @DisplayName("验证处理器只接收 PING 心跳动作")
    void supportsOnlyPingAction() {
        DevicePingProcessor processor = new DevicePingProcessor(deviceOpenInnerFacade, cachePlusOps);

        org.assertj.core.api.Assertions.assertThat(processor.supports(DeviceActionTypeEnum.PING)).isTrue();
        org.assertj.core.api.Assertions.assertThat(processor.supports(DeviceActionTypeEnum.PUBLISH)).isFalse();
    }

    @Test
    @DisplayName("验证节流到期时上报原始心跳时间并携带状态 HLC")
    void processReportsRawHeartbeatAndStatusHlcWhenThrottleDue() {
        DevicePingProcessor processor = new DevicePingProcessor(deviceOpenInnerFacade, cachePlusOps);
        when(cachePlusOps.ttl(any(CacheKey.class))).thenReturn(0L);
        when(deviceOpenInnerFacade.reportDeviceHeartbeat("client-1", 1780000000123L, 9900L))
            .thenReturn(R.success(Boolean.TRUE));

        processor.process(CommonDeviceEvent.builder()
            .actionType(DeviceActionTypeEnum.PING)
            .clientId("client-1")
            .rawMessage("{\"" + BusKafkaJsonField.HEARTBEAT_TIME + "\":1780000000123}")
            .eventHlc(9900L)
            .ts(1780000000000L)
            .build());

        verify(cachePlusOps).set(any(CacheKey.class), org.mockito.ArgumentMatchers.eq("1"));
        verify(deviceOpenInnerFacade).reportDeviceHeartbeat("client-1", 1780000000123L, 9900L);
    }

    @Test
    @DisplayName("验证节流键仍存活时只上报心跳，不重复刷新连接状态")
    void processUsesEventTsAndSkipsStatusWhenThrottleKeyIsAlive() {
        DevicePingProcessor processor = new DevicePingProcessor(deviceOpenInnerFacade, cachePlusOps);
        when(cachePlusOps.ttl(any(CacheKey.class))).thenReturn(30L);
        when(deviceOpenInnerFacade.reportDeviceHeartbeat("client-1", 1780000000000L, null))
            .thenReturn(R.success(Boolean.TRUE));

        processor.process(CommonDeviceEvent.builder()
            .actionType(DeviceActionTypeEnum.PING)
            .clientId("client-1")
            .rawMessage("{bad-json")
            .eventHlc(9900L)
            .ts(1780000000000L)
            .build());

        verify(cachePlusOps, never()).set(any(CacheKey.class), org.mockito.ArgumentMatchers.any());
        verify(deviceOpenInnerFacade).reportDeviceHeartbeat("client-1", 1780000000000L, null);
    }

    @Test
    @DisplayName("验证节流缓存异常时仍上报心跳，避免 Redis 波动影响设备活跃时间")
    void processStillReportsHeartbeatWhenThrottleCacheFails() {
        DevicePingProcessor processor = new DevicePingProcessor(deviceOpenInnerFacade, cachePlusOps);
        when(cachePlusOps.ttl(any(CacheKey.class))).thenThrow(new IllegalStateException("redis down"));
        when(deviceOpenInnerFacade.reportDeviceHeartbeat("client-1", 1780000000000L, null))
            .thenReturn(R.success(Boolean.TRUE));

        processor.process(CommonDeviceEvent.builder()
            .actionType(DeviceActionTypeEnum.PING)
            .clientId("client-1")
            .eventHlc(9900L)
            .ts(1780000000000L)
            .build());

        verify(deviceOpenInnerFacade).reportDeviceHeartbeat("client-1", 1780000000000L, null);
    }

    @Test
    @DisplayName("验证缺少心跳时间和状态时间时不调用设备服务")
    void processDoesNothingWhenNeitherHeartbeatNorStatusNeedsReporting() {
        DevicePingProcessor processor = new DevicePingProcessor(deviceOpenInnerFacade, cachePlusOps);

        processor.process(CommonDeviceEvent.builder()
            .actionType(DeviceActionTypeEnum.PING)
            .clientId("client-1")
            .build());

        verify(deviceOpenInnerFacade, never()).reportDeviceHeartbeat(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("验证心跳上报接口返回失败时抛出异常交给 bus 阶段记录失败")
    void processThrowsWhenHeartbeatFacadeReturnsFailure() {
        DevicePingProcessor processor = new DevicePingProcessor(deviceOpenInnerFacade, cachePlusOps);
        when(cachePlusOps.ttl(any(CacheKey.class))).thenReturn(0L);
        when(deviceOpenInnerFacade.reportDeviceHeartbeat("client-1", 1780000000000L, 9900L))
            .thenReturn(R.fail("facade failed"));

        assertThatThrownBy(() -> processor.process(CommonDeviceEvent.builder()
            .actionType(DeviceActionTypeEnum.PING)
            .clientId("client-1")
            .eventHlc(9900L)
            .ts(1780000000000L)
            .build()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("reportDeviceHeartbeat failed");
    }
}
