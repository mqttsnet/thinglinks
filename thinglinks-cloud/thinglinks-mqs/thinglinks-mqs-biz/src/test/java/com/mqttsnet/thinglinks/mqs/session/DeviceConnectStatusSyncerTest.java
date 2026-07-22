package com.mqttsnet.thinglinks.mqs.session;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.device.enumeration.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenInnerFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS 设备连接状态 CAS 同步器")
class DeviceConnectStatusSyncerTest {

    private final DeviceOpenInnerFacade deviceOpenInnerFacade = org.mockito.Mockito.mock(DeviceOpenInnerFacade.class);
    private final DeviceConnectStatusSyncer syncer = new DeviceConnectStatusSyncer(deviceOpenInnerFacade);

    @Test
    @DisplayName("验证按事件 HLC 写入设备在线/离线状态")
    void syncWritesConnectionStatusByEventHlc() {
        when(deviceOpenInnerFacade.updateDeviceConnectionStatusByEvent("client-1",
            DeviceConnectStatusEnum.ONLINE.getValue(), 1000L)).thenReturn(R.success(Boolean.TRUE));

        syncer.sync("client-1", DeviceConnectStatusEnum.ONLINE, 1000L);

        verify(deviceOpenInnerFacade).updateDeviceConnectionStatusByEvent("client-1",
            DeviceConnectStatusEnum.ONLINE.getValue(), 1000L);
    }

    @Test
    @DisplayName("验证缺少 clientId、状态或有效 HLC 时不调用设备服务")
    void syncSkipsBlankClientIdStatusOrInvalidHlc() {
        syncer.sync(" ", DeviceConnectStatusEnum.ONLINE, 1000L);
        syncer.sync("client-1", null, 1000L);
        syncer.sync("client-1", DeviceConnectStatusEnum.ONLINE, null);
        syncer.sync("client-1", DeviceConnectStatusEnum.ONLINE, 0L);

        verify(deviceOpenInnerFacade, never()).updateDeviceConnectionStatusByEvent(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("验证 CAS 被拒绝、接口失败和异常都不会中断事件链路")
    void syncSwallowsFacadeFailureAndCasRejectedResult() {
        when(deviceOpenInnerFacade.updateDeviceConnectionStatusByEvent("client-1",
            DeviceConnectStatusEnum.OFFLINE.getValue(), 1000L)).thenReturn(R.success(Boolean.FALSE));
        when(deviceOpenInnerFacade.updateDeviceConnectionStatusByEvent("client-1",
            DeviceConnectStatusEnum.OFFLINE.getValue(), 1001L)).thenReturn(R.fail("busy"));
        when(deviceOpenInnerFacade.updateDeviceConnectionStatusByEvent("client-1",
            DeviceConnectStatusEnum.OFFLINE.getValue(), 1002L)).thenThrow(new IllegalStateException("down"));

        syncer.sync("client-1", DeviceConnectStatusEnum.OFFLINE, 1000L);
        syncer.sync("client-1", DeviceConnectStatusEnum.OFFLINE, 1001L);
        syncer.sync("client-1", DeviceConnectStatusEnum.OFFLINE, 1002L);

        verify(deviceOpenInnerFacade).updateDeviceConnectionStatusByEvent("client-1",
            DeviceConnectStatusEnum.OFFLINE.getValue(), 1000L);
        verify(deviceOpenInnerFacade).updateDeviceConnectionStatusByEvent("client-1",
            DeviceConnectStatusEnum.OFFLINE.getValue(), 1001L);
        verify(deviceOpenInnerFacade).updateDeviceConnectionStatusByEvent("client-1",
            DeviceConnectStatusEnum.OFFLINE.getValue(), 1002L);
    }
}
