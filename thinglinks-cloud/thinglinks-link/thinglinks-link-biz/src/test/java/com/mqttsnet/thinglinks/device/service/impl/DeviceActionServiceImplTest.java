package com.mqttsnet.thinglinks.device.service.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenInnerFacade;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceActionCacheVO;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionStatusEnum;
import com.mqttsnet.thinglinks.device.manager.DeviceActionManager;
import com.mqttsnet.thinglinks.vo.query.KillClientRequestVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("设备动作服务")
class DeviceActionServiceImplTest {

    @Mock
    private LinkCacheDataHelper linkCacheDataHelper;
    @Mock
    private MqttBrokerOpenInnerFacade mqttBrokerOpenInnerFacade;
    @Mock
    private DeviceActionManager deviceActionManager;
    @Mock
    private R<?> brokerResponse;

    private DeviceActionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DeviceActionServiceImpl(linkCacheDataHelper, mqttBrokerOpenInnerFacade);
        ReflectionTestUtils.setField(service, "superManager", deviceActionManager);
    }

    @Test
    @DisplayName("手动断开设备成功时调用Broker并记录成功的断开动作")
    void disconnectDeviceShouldCallBrokerAndRecordActionWhenSuccess() {
        when(linkCacheDataHelper.getDeviceCacheVO("device-a"))
                .thenReturn(Optional.of(deviceCache()))
                .thenReturn(Optional.of(deviceCache()));
        doReturn(brokerResponse).when(mqttBrokerOpenInnerFacade).closeConnection(any(KillClientRequestVO.class));
        when(brokerResponse.getIsSuccess()).thenReturn(true);
        when(deviceActionManager.save(any(DeviceAction.class))).thenReturn(true);

        Boolean result = service.disconnectDevice("device-a");

        assertThat(result).isTrue();
        ArgumentCaptor<KillClientRequestVO> requestCaptor = ArgumentCaptor.forClass(KillClientRequestVO.class);
        verify(mqttBrokerOpenInnerFacade).closeConnection(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getTenantId()).isEqualTo("1");
        assertThat(requestCaptor.getValue().getUserId()).isEqualTo("device-a");
        assertThat(requestCaptor.getValue().getClientId()).isEqualTo("client-a");
        assertThat(requestCaptor.getValue().getClientType()).isEqualTo("web");

        ArgumentCaptor<DeviceAction> actionCaptor = ArgumentCaptor.forClass(DeviceAction.class);
        verify(deviceActionManager).save(actionCaptor.capture());
        assertThat(actionCaptor.getValue().getDeviceIdentification()).isEqualTo("device-a");
        assertThat(actionCaptor.getValue().getActionType()).isEqualTo(DeviceActionTypeEnum.DISCONNECT.getValue());
        assertThat(actionCaptor.getValue().getStatus()).isEqualTo(DeviceActionStatusEnum.SUCCESSFUL.getValue());
        assertThat(actionCaptor.getValue().getMessage()).contains("\"clientId\":\"client-a\"");
        verify(linkCacheDataHelper).setDeviceActionCacheVO(eq("product-a"), eq("device-a"),
                any(DeviceActionCacheVO.class));
    }

    @Test
    @DisplayName("Broker断开失败时返回失败结果且不写入设备动作记录")
    void disconnectDeviceShouldNotRecordActionWhenBrokerFails() {
        when(linkCacheDataHelper.getDeviceCacheVO("device-a")).thenReturn(Optional.of(deviceCache()));
        doReturn(brokerResponse).when(mqttBrokerOpenInnerFacade).closeConnection(any(KillClientRequestVO.class));
        when(brokerResponse.getIsSuccess()).thenReturn(false);

        Boolean result = service.disconnectDevice("device-a");

        assertThat(result).isFalse();
        verify(deviceActionManager, never()).save(any(DeviceAction.class));
        verify(linkCacheDataHelper, never()).setDeviceActionCacheVO(any(), any(), any());
    }

    @Test
    @DisplayName("设备缓存不存在时直接抛出业务异常并不调用Broker")
    void disconnectDeviceShouldFailFastWhenDeviceMissing() {
        when(linkCacheDataHelper.getDeviceCacheVO("device-a")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.disconnectDevice("device-a"));

        verify(mqttBrokerOpenInnerFacade, never()).closeConnection(any(KillClientRequestVO.class));
    }

    private DeviceCacheVO deviceCache() {
        return new DeviceCacheVO()
                .setTenantId(1L)
                .setClientId("client-a")
                .setProductIdentification("product-a")
                .setDeviceIdentification("device-a");
    }
}
