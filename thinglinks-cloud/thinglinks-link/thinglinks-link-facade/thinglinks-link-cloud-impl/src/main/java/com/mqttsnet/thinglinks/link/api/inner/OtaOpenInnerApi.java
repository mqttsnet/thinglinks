package com.mqttsnet.thinglinks.link.api.inner;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.link.api.inner.hystrix.OtaOpenInnerApiFallback;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaListUpgradeableVersionsResponseParam;
import com.mqttsnet.thinglinks.ota.vo.param.DeviceOtaUpgradeAppConfirmationParam;
import com.mqttsnet.thinglinks.ota.vo.result.DeviceOtaUpgradeAppConfirmationResultVO;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaCommandResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReadResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportResponseParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Description:
 * OTA相关开放接口-API
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/12/26
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:thinglinks-link-server}",
        fallback = OtaOpenInnerApiFallback.class, path = "/inner/otaOpen")
public interface OtaOpenInnerApi {

    /**
     * Handles app confirmation for device OTA upgrade.
     *
     * @param param The device OTA upgrade app confirmation parameters.
     * @return {@link R<DeviceOtaUpgradeAppConfirmationResultVO>} A response wrapper indicating the success of the app confirmation.
     */
    @PostMapping("/otaUpgradeAppConfirmation")
    R<DeviceOtaUpgradeAppConfirmationResultVO> otaUpgradeAppConfirmation(@RequestBody DeviceOtaUpgradeAppConfirmationParam param);

    /**
     * MQTT协议保存OTA升级记录
     */
    @Operation(summary = "MQTT协议保存OTA升级记录", description = "MQTT协议保存OTA升级记录")
    @PostMapping(path = "/saveOtaUpgradeRecordByMqtt", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoOtaCommandResponseParam> saveOtaUpgradeRecordByMqtt(@Valid @RequestBody TopoOtaCommandResponseParam topoOtaCommandResponseParam);

    /**
     * 北向API-保存OTA升级记录
     */
    @Operation(summary = "北向API保存OTA升级记录", description = "北向API-保存OTA升级记录")
    @PostMapping(path = "/saveOtaUpgradeRecordByNorthbound", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoOtaCommandResponseParam> saveOtaUpgradeRecordByNorthbound(@Valid @RequestBody TopoOtaCommandResponseParam topoOtaCommandResponseParam);

    /**
     * MQTT协议拉取OTA信息
     */
    @Operation(summary = "MQTT协议拉取OTA信息", description = "MQTT协议拉取OTA信息")
    @PostMapping(path = "/otaPullByMqtt", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoOtaPullResponseParam> otaPullByMqtt(@Valid @RequestBody TopoOtaPullParam topoOtaPullParam);

    /**
     * 北向API-OTA拉取软固件信息
     */
    @Operation(summary = "北向API-OTA拉取软固件信息", description = "北向API-OTA拉取软固件信息")
    @PostMapping(path = "/otaPullByNorthbound", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoOtaPullResponseParam> otaPullByNorthbound(@Valid @RequestBody TopoOtaPullParam topoOtaPullParam);

    /**
     * MQTT协议上报OTA信息
     */
    @Operation(summary = "MQTT协议上报OTA信息", description = "MQTT协议上报OTA信息")
    @PostMapping(path = "/otaReportByMqtt", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoOtaReportResponseParam> otaReportByMqtt(@Valid @RequestBody TopoOtaReportParam topoOtaReportParam);

    /**
     * 北向API-OTA上报软固件版本
     */
    @Operation(summary = "北向API-OTA上报软固件版本", description = "北向API-OTA上报软固件版本")
    @PostMapping(path = "/otaReportByNorthbound", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoOtaReportResponseParam> otaReportByNorthbound(@Valid @RequestBody TopoOtaReportParam topoOtaReportParam);

    /**
     * MQTT协议OTA读取响应
     */
    @Operation(summary = "MQTT协议OTA读取响应", description = "MQTT协议OTA读取响应")
    @PostMapping(path = "/otaReadResponseByMqtt", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<?> otaReadResponseByMqtt(@Valid @RequestBody TopoOtaReadResponseParam topoOtaReadResponseParam);

    /**
     * 北向API-OTA读取设备软固件版本信息响应
     */
    @Operation(summary = "北向API-OTA读取设备软固件版本信息响应", description = "北向API-OTA读取设备软固件版本信息响应")
    @PostMapping(path = "/otaReadResponseByNorthbound", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<?> otaReadResponseByNorthbound(@Valid @RequestBody TopoOtaReadResponseParam topoOtaReadResponseParam);

    /**
     * 北向API-OTA获取可升级版本列表
     */
    @Operation(summary = "北向API-OTA获取可升级版本列表", description = "北向API-OTA获取可升级版本列表")
    @GetMapping(path = "/getAvailableUpgradeVersions")
    R<TopoOtaListUpgradeableVersionsResponseParam> getAvailableUpgradeVersionsByNorthbound(
            @Parameter(description = "设备标识") @RequestParam("deviceIdentification") String deviceIdentification,
            @Parameter(description = "包类型") @RequestParam("packageType") Integer packageType);

}
