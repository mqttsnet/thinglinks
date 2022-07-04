package com.mqttsnet.thinglinks.link.controller.device;

import com.mqttsnet.thinglinks.common.core.utils.SecurityUtils;
import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceInfo;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 子设备管理Controller
 *
 * @author thinglinks
 * @date 2022-06-21
 */
@RestController
@RequestMapping("/deviceInfo")
public class DeviceInfoController extends BaseController
{
    @Autowired
    private DeviceInfoService deviceInfoService;

    /**
     * 查询子设备管理列表
     */
    @PreAuthorize(hasPermi = "link:deviceInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(DeviceInfo deviceInfo)
    {
        startPage();
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(deviceInfo);
        return getDataTable(list);
    }

    /**
     * 导出子设备管理列表
     */
    @PreAuthorize(hasPermi = "link:deviceInfo:export")
    @Log(title = "子设备管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DeviceInfo deviceInfo) throws IOException
    {
        List<DeviceInfo> list = deviceInfoService.selectDeviceInfoList(deviceInfo);
        ExcelUtil<DeviceInfo> util = new ExcelUtil<DeviceInfo>(DeviceInfo.class);
        util.exportExcel(response, list, "子设备管理数据");
    }

    /**
     * 获取子设备管理详细信息
     */
    @PreAuthorize(hasPermi = "link:deviceInfo:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(deviceInfoService.selectDeviceInfoById(id));
    }

    /**
     * 新增子设备管理
     */
    @PreAuthorize(hasPermi = "link:deviceInfo:add")
    @Log(title = "子设备管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DeviceInfo deviceInfo)
    {
        deviceInfo.setCreateBy(SecurityUtils.getUsername());
        return toAjax(deviceInfoService.insertDeviceInfo(deviceInfo));
    }

    /**
     * 修改子设备管理
     */
    @PreAuthorize(hasPermi = "link:deviceInfo:edit")
    @Log(title = "子设备管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DeviceInfo deviceInfo)
    {
        deviceInfo.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(deviceInfoService.updateDeviceInfo(deviceInfo));
    }

    /**
     * 删除子设备管理
     */
    @PreAuthorize(hasPermi = "link:deviceInfo:remove")
    @Log(title = "子设备管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(deviceInfoService.deleteDeviceInfoByIds(ids));
    }

    /**
     * 查询子设备影子数据
     * @param params
     * @return
     */
    @PreAuthorize(hasPermi = "link:deviceInfo:shadow")
    @PostMapping(value = "/getDeviceInfoShadow")
    public AjaxResult getDeviceInfoShadow(@RequestBody Map<String, Object> params)
    {
        final Object ids = params.get("ids");
        final Object startTime = params.get("startTime");
        final Object endTime = params.get("endTime");
        return AjaxResult.success(deviceInfoService.getDeviceInfoShadow(ids.toString(), startTime.toString(), endTime.toString()));
    }
}
