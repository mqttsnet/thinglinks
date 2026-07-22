package com.mqttsnet.thinglinks.mqs.event.hook;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.mqs.event.hook.impl.DeviceConnectStatusSyncHook;
import com.mqttsnet.thinglinks.mqs.session.DeviceConnectStatusSyncer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS 设备连接状态同步 Hook")
class DeviceConnectStatusSyncHookTest {

    private final DeviceConnectStatusSyncer syncer = org.mockito.Mockito.mock(DeviceConnectStatusSyncer.class);
    private final DeviceConnectStatusSyncHook hook = new DeviceConnectStatusSyncHook(syncer);

    @Test
    @DisplayName("验证 Hook 只支持会改变连接状态的设备动作")
    void supportsOnlyConnectionStatusActions() {
        org.assertj.core.api.Assertions.assertThat(hook.supports(DeviceActionTypeEnum.CONNECT)).isTrue();
        org.assertj.core.api.Assertions.assertThat(hook.supports(DeviceActionTypeEnum.DISCONNECT)).isTrue();
        org.assertj.core.api.Assertions.assertThat(hook.supports(DeviceActionTypeEnum.PUBLISH)).isFalse();
        org.assertj.core.api.Assertions.assertThat(hook.supports((DeviceActionTypeEnum) null)).isFalse();
    }

    @Test
    @DisplayName("验证 CONNECT 事件按 eventHlc 写入在线状态")
    void beforeDispatchMapsConnectToOnlineWithEventHlc() {
        hook.beforeDispatch(ctx(DeviceActionTypeEnum.CONNECT, 1000L, 900L));

        verify(syncer).sync("client-1", DeviceConnectStatusEnum.ONLINE, 1000L);
    }

    @Test
    @DisplayName("验证断连/踢下线等动作统一写入离线状态")
    void beforeDispatchMapsDisconnectLikeActionsToOffline() {
        hook.beforeDispatch(ctx(DeviceActionTypeEnum.KICKED, 2000L, 900L));

        verify(syncer).sync("client-1", DeviceConnectStatusEnum.OFFLINE, 2000L);
    }

    @Test
    @DisplayName("验证缺少 eventHlc 时使用事件时间兜底同步状态")
    void beforeDispatchFallsBackToEventUtcWhenHlcMissing() {
        hook.beforeDispatch(ctx(DeviceActionTypeEnum.ERROR, null, 900L));

        verify(syncer).sync("client-1", DeviceConnectStatusEnum.OFFLINE, 900L);
    }

    @Test
    @DisplayName("验证分发器按 supports 过滤后，非连接状态动作不会调用同步器")
    void unsupportedActionDoesNotCallSyncerWhenDispatcherFiltersBySupports() {
        DeviceEventContext ctx = ctx(DeviceActionTypeEnum.PUBLISH, 1000L, 900L);
        if (hook.supports(ctx)) {
            hook.beforeDispatch(ctx);
        }

        verify(syncer, never()).sync(org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private static DeviceEventContext ctx(DeviceActionTypeEnum actionType, Long eventHlc, Long ts) {
        return new DeviceEventContext(CommonDeviceEvent.builder()
            .actionType(actionType)
            .clientId("client-1")
            .eventHlc(eventHlc)
            .ts(ts)
            .build(), System.currentTimeMillis());
    }
}
