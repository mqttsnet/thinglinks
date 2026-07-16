package com.mqttsnet.thinglinks.mqs.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionStatusEnum;
import com.mqttsnet.thinglinks.device.vo.save.DeviceActionSaveVO;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenInnerFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("MQS 设备动作记录服务")
class DeviceEventActionServiceImplTest {

    private final DeviceOpenInnerFacade deviceOpenInnerFacade = org.mockito.Mockito.mock(DeviceOpenInnerFacade.class);
    private DeviceEventActionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DeviceEventActionServiceImpl();
        ReflectionTestUtils.setField(service, "deviceOpenInnerApi", deviceOpenInnerFacade);
    }

    @Test
    @DisplayName("验证统一设备事件可转换为设备动作记录并保存")
    void saveBuildsDeviceActionRecordFromCommonEvent() {
        when(deviceOpenInnerFacade.saveDeviceAction(org.mockito.ArgumentMatchers.any()))
            .thenReturn(R.success(new DeviceAction()));
        CommonDeviceEvent event = CommonDeviceEvent.builder()
            .actionType(DeviceActionTypeEnum.PING)
            .clientId("client-1")
            .deviceIdentification("device-1")
            .rawMessage("{\"type\":\"PING\"}")
            .build();

        service.save(event);

        ArgumentCaptor<DeviceActionSaveVO> captor = ArgumentCaptor.forClass(DeviceActionSaveVO.class);
        verify(deviceOpenInnerFacade).saveDeviceAction(captor.capture());
        DeviceActionSaveVO vo = captor.getValue();
        assertThat(vo.getDeviceIdentification()).isEqualTo("device-1");
        assertThat(vo.getActionType()).isEqualTo(DeviceActionTypeEnum.PING.getValue());
        assertThat(vo.getMessage()).isEqualTo("{\"type\":\"PING\"}");
        assertThat(vo.getStatus()).isEqualTo(DeviceActionStatusEnum.SUCCESSFUL.getValue());
        assertThat(vo.getRemark()).isEqualTo(DeviceActionTypeEnum.PING.getDesc());
    }

    @Test
    @DisplayName("验证空事件、缺少动作或缺少设备标识时不写动作记录")
    void saveSkipsNullEventMissingActionOrMissingDeviceIdentification() {
        service.save(null);
        service.save(CommonDeviceEvent.builder().deviceIdentification("device-1").build());
        service.save(CommonDeviceEvent.builder()
            .actionType(DeviceActionTypeEnum.CONNECT)
            .clientId("client-1")
            .build());

        verify(deviceOpenInnerFacade, never()).saveDeviceAction(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("验证设备动作保存失败或异常时不阻断 MQS 主流程")
    void saveDoesNotBlockWhenFacadeFailsOrThrows() {
        when(deviceOpenInnerFacade.saveDeviceAction(org.mockito.ArgumentMatchers.any()))
            .thenReturn(R.fail("busy"))
            .thenThrow(new IllegalStateException("down"));

        service.save(event("device-1"));
        service.save(event("device-2"));

        verify(deviceOpenInnerFacade, org.mockito.Mockito.times(2))
            .saveDeviceAction(org.mockito.ArgumentMatchers.any());
    }

    private static CommonDeviceEvent event(String deviceIdentification) {
        return CommonDeviceEvent.builder()
            .actionType(DeviceActionTypeEnum.CONNECT)
            .clientId("client-1")
            .deviceIdentification(deviceIdentification)
            .rawMessage("{}")
            .build();
    }
}
