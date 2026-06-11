package com.mqttsnet.thinglinks.device.controller;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.device.service.DeviceShadowService;
import com.mqttsnet.thinglinks.device.vo.query.DeviceShadowPageQuery;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * 设备影子信息
 * </p>
 *
 * @author mqttsnet
 * @date 2023-10-12 19:39:59
 * @create [2023-10-12 19:39:59] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/deviceShadow")
@Tag(name = "设备影子信息")
public class DeviceShadowController {

    // 常量表示 60分钟的纳秒数
    private static final long TEN_MINUTES_IN_NANOSECONDS = 60 * 60 * 1_000_000_000L;


    @Autowired
    private DeviceShadowService deviceShadowService;

    @Autowired
    private EchoService echoService;

    /**
     * 查询设备影子信息
     *
     * @param deviceIdentification 设备标识。必填参数，用于标识需要查询的设备。
     * @param startTime            开始时间戳。选填参数，用于指定查询的起始时间。格式：19位纳秒时间戳。例如，1622552643000000000表示2021年6月1日17时24分3秒（UTC时间）。
     * @param endTime              结束时间戳。选填参数，用于指定查询的结束时间。格式：19位纳秒时间戳。例如，1622552643000000000表示2021年6月1日17时24分3秒（UTC时间）。
     * @return {@link R<ProductResultVO>} 设备影子信息
     */
    @Operation(summary = "查询设备影子", description = "查询设备影子信息")
    @GetMapping("/queryDeviceShadow")
    public R<ProductResultVO> queryDeviceShadow(
            @Parameter(description = "设备标识，必填参数，用于标识需要查询的设备。", required = true, example = "7939700746264577")
            @RequestParam(value = "deviceIdentification") String deviceIdentification,

            @Parameter(description = "开始时间戳，选填参数，用于指定查询的起始时间。格式：19位纳秒时间戳。例如，1622552643000000000表示2021年6月1日17时24分3秒（UTC时间）。", example = "1622552643000000000")
            @RequestParam(value = "startTime", required = false) Long startTime,

            @Parameter(description = "结束时间戳，选填参数，用于指定查询的结束时间。格式：19位纳秒时间戳。例如，1622552643000000000表示2021年6月1日17时24分3秒（UTC时间）。", example = "1622552643000000000")
            @RequestParam(value = "endTime", required = false) Long endTime,

            @Parameter(description = "服务编码，选填参数，用于指定查询的服务代码。如果不传，则查询产品下所有服务。", example = "serviceCode1")
            @RequestParam(value = "serviceCode", required = false) String serviceCode,

            @Parameter(description = "产品版本序号,选填。不传默认按设备绑定版本查;传入则按指定历史版本快照解析物模型 + 读对应 TD 子表(用户回看上一版本影子)。", example = "1900512345678901")
            @RequestParam(value = "versionNo", required = false) String versionNo) {
        log.info("查询设备影子,设备标识: {}, 开始时间戳: {}, 结束时间戳: {}, 服务编码: {}, 版本: {}",
                deviceIdentification, startTime, endTime, serviceCode, versionNo);
        ArgumentAssert.isTrue((startTime == null && endTime == null) || (startTime != null && endTime != null),
                "开始时间和结束时间必须同时提供，或者都不提供。");
        if (startTime != null) {
            long timeDifference = endTime - startTime;
            ArgumentAssert.isTrue(timeDifference <= TEN_MINUTES_IN_NANOSECONDS, "时间范围不能超过60分钟");
        }
        try {
            DeviceShadowPageQuery deviceShadowPageQuery = new DeviceShadowPageQuery();
            deviceShadowPageQuery.setDeviceIdentification(deviceIdentification);
            deviceShadowPageQuery.setStartTime(startTime);
            deviceShadowPageQuery.setEndTime(endTime);
            deviceShadowPageQuery.setServiceCode(serviceCode);
            deviceShadowPageQuery.setVersionNo(versionNo);
            ProductResultVO result = deviceShadowService.queryDeviceShadow(deviceShadowPageQuery);
            echoService.action(result);
            return R.success(result);
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("查询设备影子失败，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }


}


