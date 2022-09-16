package com.mqttsnet.thinglinks.link.controller.device;

import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
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
public class DeviceShadowController extends BaseController
{
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceInfoService deviceInfoService;

    /**
     * 查询普通设备影子数据
     * @param params
     * @return
     */
    @PreAuthorize(hasPermi = "link:shadow:device")
    @PostMapping(value = "/getDeviceShadow")
    public AjaxResult getDeviceShadow(@RequestBody Map<String, Object> params)
    {
        final Object ids = params.get("ids");
        final Object startTime = params.get("startTime");
        final Object endTime = params.get("endTime");
        return AjaxResult.success(deviceService.getDeviceShadow(ids.toString(), startTime.toString(), endTime.toString()));
    }

    /**
     * 查询子设备影子数据
     * @param params
     * @return
     */
    @PreAuthorize(hasPermi = "link:shadow:deviceInfo")
    @PostMapping(value = "/getDeviceInfoShadow")
    public AjaxResult getDeviceInfoShadow(@RequestBody Map<String, Object> params)
    {
        final Object ids = params.get("ids");
        final Object startTime = params.get("startTime");
        final Object endTime = params.get("endTime");
        return AjaxResult.success(deviceInfoService.getDeviceInfoShadow(ids.toString(), startTime.toString(), endTime.toString()));
    }

}
