package com.mqttsnet.thinglinks.ota.service.support;

import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.ota.enumeration.OtaPackageTypeEnum;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OTA物模型版本切换")
class OtaModelVersionSwitcherTest {

    @Mock
    private DeviceService deviceService;
    @Mock
    private OtaUpgradesService otaUpgradesService;

    @Test
    @DisplayName("升级成功时按升级包目标版本切换单台设备绑定的产品版本")
    void switchOnUpgradeSuccessShouldSwitchDeviceVersion() {
        OtaModelVersionSwitcher switcher = new OtaModelVersionSwitcher(deviceService, otaUpgradesService);

        switcher.switchOnUpgradeSuccess("product-a", "device-a", "v2-shadow");

        verify(deviceService).switchBoundProductVersion("product-a", List.of("device-a"), "v2-shadow");
    }

    @Test
    @DisplayName("升级成功缺少产品、设备或目标版本时不驱动版本切换")
    void switchOnUpgradeSuccessShouldSkipBlankInputs() {
        OtaModelVersionSwitcher switcher = new OtaModelVersionSwitcher(deviceService, otaUpgradesService);

        switcher.switchOnUpgradeSuccess("product-a", "device-a", "");
        switcher.switchOnUpgradeSuccess("product-a", "", "v2-shadow");
        switcher.switchOnUpgradeSuccess("", "device-a", "v2-shadow");

        verify(deviceService, never()).switchBoundProductVersion(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("版本上报时先按产品和上报版本解析目标产品版本再切换")
    void syncByReportedVersionShouldResolveAndSwitch() {
        when(otaUpgradesService.resolveProductVersionNo("product-a", "2.0.1",
                OtaPackageTypeEnum.FIRMWARE.getValue())).thenReturn("v2-shadow");
        OtaModelVersionSwitcher switcher = new OtaModelVersionSwitcher(deviceService, otaUpgradesService);

        switcher.syncByReportedVersion("product-a", "device-a", "2.0.1",
                OtaPackageTypeEnum.FIRMWARE.getValue());

        verify(otaUpgradesService).resolveProductVersionNo("product-a", "2.0.1",
                OtaPackageTypeEnum.FIRMWARE.getValue());
        verify(deviceService).switchBoundProductVersion("product-a", List.of("device-a"), "v2-shadow");
    }

    @Test
    @DisplayName("底层版本切换失败时只记录告警并保持OTA主流程不中断")
    void switchShouldBeFailSoftWhenDeviceServiceThrows() {
        when(deviceService.switchBoundProductVersion("product-a", List.of("device-a"), "v2-shadow"))
                .thenThrow(new IllegalStateException("目标版本未发布"));
        OtaModelVersionSwitcher switcher = new OtaModelVersionSwitcher(deviceService, otaUpgradesService);

        assertDoesNotThrow(() -> switcher.switchOnUpgradeSuccess("product-a", "device-a", "v2-shadow"));
    }
}
