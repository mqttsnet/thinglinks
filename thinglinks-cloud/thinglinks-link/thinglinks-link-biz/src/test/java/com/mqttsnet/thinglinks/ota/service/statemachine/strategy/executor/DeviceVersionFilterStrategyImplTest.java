package com.mqttsnet.thinglinks.ota.service.statemachine.strategy.executor;

import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeTasksResultDTO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradesResultDTO;
import com.mqttsnet.thinglinks.ota.enumeration.OtaPackageTypeEnum;
import com.mqttsnet.thinglinks.ota.service.statemachine.strategy.executor.impl.DeviceVersionFilterStrategyImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OTA设备版本过滤策略")
class DeviceVersionFilterStrategyImplTest {

    private final DeviceVersionFilterStrategyImpl strategy = new DeviceVersionFilterStrategyImpl();

    @Test
    @DisplayName("源版本为空时不过滤设备，支持全量升级任务")
    void isVersionMatchShouldPassWhenSourceVersionsBlank() {
        OtaUpgradeTasksResultDTO task = task(OtaPackageTypeEnum.SOFTWARE.getValue(), " ");
        DeviceResultVO device = new DeviceResultVO().setDeviceIdentification("device-a");

        assertThat(strategy.isVersionMatch(task, device)).isTrue();
    }

    @Test
    @DisplayName("软件包升级时使用设备软件版本匹配源版本列表")
    void isVersionMatchShouldUseSoftwareVersion() {
        OtaUpgradeTasksResultDTO task = task(OtaPackageTypeEnum.SOFTWARE.getValue(), "1.0.0, 1.1.0");
        DeviceResultVO device = new DeviceResultVO()
                .setDeviceIdentification("device-a")
                .setSwVersion("1.1.0")
                .setFwVersion("9.9.9");

        assertThat(strategy.isVersionMatch(task, device)).isTrue();
    }

    @Test
    @DisplayName("固件包升级时使用设备固件版本，版本不在源列表内则不匹配")
    void isVersionMatchShouldUseFirmwareVersion() {
        OtaUpgradeTasksResultDTO task = task(OtaPackageTypeEnum.FIRMWARE.getValue(), "1.0.0, 1.1.0");
        DeviceResultVO device = new DeviceResultVO()
                .setDeviceIdentification("device-a")
                .setSwVersion("1.1.0")
                .setFwVersion("2.0.0");

        assertThat(strategy.isVersionMatch(task, device)).isFalse();
    }

    @Test
    @DisplayName("构建设备查询条件时软件包写入软件版本列表")
    void buildVersionFilterQueryShouldSetSoftwareVersionList() {
        DevicePageQuery query = new DevicePageQuery();

        DevicePageQuery result = strategy.buildVersionFilterQuery(query,
                task(OtaPackageTypeEnum.SOFTWARE.getValue(), "1.0.0, 1.1.0"));

        assertThat(result.getSwVersionList()).containsExactly("1.0.0", "1.1.0");
        assertThat(result.getFwVersionList()).isNull();
    }

    @Test
    @DisplayName("构建设备查询条件时固件包写入固件版本列表")
    void buildVersionFilterQueryShouldSetFirmwareVersionList() {
        DevicePageQuery query = new DevicePageQuery();

        DevicePageQuery result = strategy.buildVersionFilterQuery(query,
                task(OtaPackageTypeEnum.FIRMWARE.getValue(), "2.0.0, 2.1.0"));

        assertThat(result.getFwVersionList()).containsExactly("2.0.0", "2.1.0");
        assertThat(result.getSwVersionList()).isNull();
    }

    @Test
    @DisplayName("解析源版本时会去除空项和前后空格")
    void parseSourceVersionsShouldTrimAndDropBlankItems() {
        assertThat(strategy.parseSourceVersions(" 1.0.0, ,2.0.0 ,, "))
                .containsExactly("1.0.0", "2.0.0");
    }

    private OtaUpgradeTasksResultDTO task(Integer packageType, String sourceVersions) {
        return new OtaUpgradeTasksResultDTO()
                .setId(1L)
                .setSourceVersions(sourceVersions)
                .setOtaUpgradesResult(new OtaUpgradesResultDTO().setPackageType(packageType));
    }
}
