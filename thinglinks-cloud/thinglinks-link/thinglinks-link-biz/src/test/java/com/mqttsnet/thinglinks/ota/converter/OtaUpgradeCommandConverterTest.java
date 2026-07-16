package com.mqttsnet.thinglinks.ota.converter;

import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeFileResultDTO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeTasksResultDTO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradesResultDTO;
import com.mqttsnet.thinglinks.ota.enumeration.OtaPackageSignMethodEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaPackageTypeEnum;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaCommandRequestParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullResponseParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OTA升级命令转换")
class OtaUpgradeCommandConverterTest {

    @Test
    @DisplayName("构建下发命令时解析文件ID、去重URL并按签名方式取首个有效签名")
    void buildOtaCommandRequestParamShouldResolveFilesAndSign() {
        OtaUpgradeTasksResultDTO task = upgradeTask();
        OtaUpgradesResultDTO upgradePackage = upgradePackage()
                .setFileLocation("10, invalid, 11, 10, 999")
                .setSignMethod(OtaPackageSignMethodEnum.SHA256.getValue());
        Map<Long, OtaUpgradeFileResultDTO> files = Map.of(
                10L, new OtaUpgradeFileResultDTO()
                        .setUrl("https://cdn.example.com/ota-a.bin")
                        .setFileMd5("md5-a")
                        .setFileSha256("sha256-a"),
                11L, new OtaUpgradeFileResultDTO()
                        .setUrl("https://cdn.example.com/ota-b.bin")
                        .setFileMd5("md5-b")
                        .setFileSha256("sha256-b")
        );

        TopoOtaCommandRequestParam result = OtaUpgradeCommandConverter
                .buildOtaCommandRequestParam("device-a", task, upgradePackage, files);

        assertThat(result.getDeviceIdentification()).isEqualTo("device-a");
        assertThat(result.getProductIdentification()).isEqualTo("product-a");
        assertThat(result.getOtaTaskId()).isEqualTo(1001L);
        assertThat(result.getOtaTaskName()).isEqualTo("灰度升级任务");
        assertThat(result.getPackageName()).isEqualTo("主控固件");
        assertThat(result.getPackageType()).isEqualTo(OtaPackageTypeEnum.SOFTWARE.getValue());
        assertThat(result.getVersion()).isEqualTo("2.0.1");
        assertThat(result.getFileLocation()).isEqualTo("https://cdn.example.com/ota-a.bin,https://cdn.example.com/ota-b.bin");
        assertThat(result.getSignMethod()).isEqualTo(OtaPackageSignMethodEnum.SHA256.getValue());
        assertThat(result.getSign()).isEqualTo("sha256-a");
        assertThat(result.getDescription()).isEqualTo("修复离线重连");
        assertThat(result.getCustomInfo()).isEqualTo("{\"batch\":\"gray\"}");
    }

    @Test
    @DisplayName("构建拉取响应时文件映射为空也返回稳定空地址和空签名")
    void buildOtaPullResponseParamShouldHandleMissingFileMap() {
        OtaUpgradesResultDTO upgradePackage = upgradePackage()
                .setFileLocation("10")
                .setSignMethod(OtaPackageSignMethodEnum.MD5.getValue());

        TopoOtaPullResponseParam result = OtaUpgradeCommandConverter
                .buildOtaPullResponseParam("device-a", upgradeTask(), upgradePackage, null);

        assertThat(result.getFileLocation()).isEmpty();
        assertThat(result.getSign()).isNull();
        assertThat(result.getPackageName()).isEqualTo("主控固件");
    }

    private OtaUpgradeTasksResultDTO upgradeTask() {
        return new OtaUpgradeTasksResultDTO()
                .setId(1001L)
                .setTaskName("灰度升级任务");
    }

    private OtaUpgradesResultDTO upgradePackage() {
        return new OtaUpgradesResultDTO()
                .setProductIdentification("product-a")
                .setPackageName("主控固件")
                .setPackageType(OtaPackageTypeEnum.SOFTWARE.getValue())
                .setVersion("2.0.1")
                .setDescription("修复离线重连")
                .setCustomInfo("{\"batch\":\"gray\"}");
    }
}
