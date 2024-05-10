package com.mqttsnet.thinglinks.link.controller.device;

import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.query.DeviceShadowPageQuery;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.device.DeviceShadowService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备影子管理Controller
 *
 * @author thinglinks
 * @date 2022-09-16
 */
@RestController
@RequestMapping("/shadow")
public class DeviceShadowController extends BaseController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceInfoService deviceInfoService;

    @Autowired
    private DeviceShadowService deviceShadowService;

    /**
     * 查询普通设备影子数据
     *
     * @param params
     * @return
     */
    @Deprecated
    @PreAuthorize(hasPermi = "link:shadow:device")
    @PostMapping(value = "/getDeviceShadow")
    public AjaxResult getDeviceShadow(@RequestBody Map<String, Object> params) {
        final Object ids = params.get("ids");
        final Object startTime = params.get("startTime");
        final Object endTime = params.get("endTime");
        return AjaxResult.success(deviceService.getDeviceShadow(ids.toString(), startTime.toString(), endTime.toString()));
    }

    /**
     * 查询子设备影子数据
     *
     * @param params
     * @return
     */
    @Deprecated
    @PreAuthorize(hasPermi = "link:shadow:deviceInfo")
    @PostMapping(value = "/getDeviceInfoShadow")
    public AjaxResult getDeviceInfoShadow(@RequestBody Map<String, Object> params) {
        final Object ids = params.get("ids");
        final Object startTime = params.get("startTime");
        final Object endTime = params.get("endTime");
        return AjaxResult.success(deviceInfoService.getDeviceInfoShadow(ids.toString(), startTime.toString(), endTime.toString()));
    }

    /**
     * 查询设备影子信息
     *
     * @param deviceIdentification 设备标识。必填参数，用于标识需要查询的设备。
     * @param startTime            开始时间戳。选填参数，用于指定查询的起始时间。格式：13位毫秒时间戳。
     * @param endTime              结束时间戳。选填参数，用于指定查询的结束时间。格式：13位毫秒时间戳。
     * @return 设备影子信息
     */
    @ApiOperation(value = "查询设备影子", httpMethod = "GET", notes = "查询设备影子信息")
    @GetMapping("/queryDeviceShadow")
    public AjaxResult queryDeviceShadow(
            @ApiParam(value = "设备标识，必填参数，用于标识需要查询的设备。", required = true, example = "7939700746264577")
            @RequestParam(value = "deviceIdentification") String deviceIdentification,

            @ApiParam(value = "开始时间戳，选填参数，用于指定查询的起始时间。格式：13位毫秒时间戳。例如，1622552643000表示2021年6月1日17时24分3秒（UTC时间）。", example = "1622552643000")
            @RequestParam(value = "startTime", required = false) Long startTime,

            @ApiParam(value = "结束时间戳，选填参数，用于指定查询的结束时间。格式：13位毫秒时间戳。例如，1622552643000表示2021年6月1日17时24分3秒（UTC时间）。", example = "1622552643000")
            @RequestParam(value = "endTime", required = false) Long endTime) {

        DeviceShadowPageQuery deviceShadowPageQuery = new DeviceShadowPageQuery();
        deviceShadowPageQuery.setDeviceIdentification(deviceIdentification);
        deviceShadowPageQuery.setStartTime(startTime);
        deviceShadowPageQuery.setEndTime(endTime);

        return AjaxResult.success(deviceShadowService.queryDeviceShadow(deviceShadowPageQuery));
    }

}
