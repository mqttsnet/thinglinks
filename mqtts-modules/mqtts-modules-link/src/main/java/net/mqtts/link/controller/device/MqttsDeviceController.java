package net.mqtts.link.controller.device;

import java.util.List;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import net.mqtts.common.core.domain.R;
import net.mqtts.common.core.utils.poi.ExcelUtil;
import net.mqtts.common.core.web.controller.BaseController;
import net.mqtts.common.core.web.domain.AjaxResult;
import net.mqtts.common.core.web.page.TableDataInfo;
import net.mqtts.common.log.annotation.Log;
import net.mqtts.common.log.enums.BusinessType;
import net.mqtts.common.security.annotation.PreAuthorize;
import net.mqtts.link.api.domain.MqttsDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import net.mqtts.link.service.device.MqttsDeviceService;

/**
 * 设备管理Controller
 *
 * @author mqtts
 * @date 2021-10-22
 */
@RestController
@RequestMapping("/device")
public class MqttsDeviceController extends BaseController {
    @Autowired
    private MqttsDeviceService mqttsDeviceService;

    /**
     * 查询设备管理列表
     */
    @PreAuthorize(hasPermi = "link:device:list")
    @GetMapping("/list")
    public TableDataInfo list(MqttsDevice mqttsDevice) {
        startPage();
        List<MqttsDevice> list = mqttsDeviceService.selectMqttsDeviceList(mqttsDevice);
        return getDataTable(list);
    }

    /**
     * 导出设备管理列表
     */
    @PreAuthorize(hasPermi = "link:device:export")
    @Log(title = "设备管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MqttsDevice mqttsDevice) throws IOException {
        List<MqttsDevice> list = mqttsDeviceService.selectMqttsDeviceList(mqttsDevice);
        ExcelUtil<MqttsDevice> util = new ExcelUtil<MqttsDevice>(MqttsDevice.class);
        util.exportExcel(response, list, "设备管理数据");
    }

    /**
     * 获取设备管理详细信息
     */
    @PreAuthorize(hasPermi = "link:device:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(mqttsDeviceService.selectMqttsDeviceById(id));
    }

    /**
     * 新增设备管理
     */
    @PreAuthorize(hasPermi = "link:device:add")
    @Log(title = "设备管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MqttsDevice mqttsDevice) {
        return toAjax(mqttsDeviceService.insertMqttsDevice(mqttsDevice));
    }

    /**
     * 修改设备管理
     */
    @PreAuthorize(hasPermi = "link:device:edit")
    @Log(title = "设备管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MqttsDevice mqttsDevice) {
        return toAjax(mqttsDeviceService.updateMqttsDevice(mqttsDevice));
    }

    /**
     * 删除设备管理
     */
    @PreAuthorize(hasPermi = "link:device:remove")
    @Log(title = "设备管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(mqttsDeviceService.deleteMqttsDeviceByIds(ids));
    }

    /**
     * 更新设备在线状态
     */
    @Log(title = "设备管理", businessType = BusinessType.UPDATE)
    @PutMapping("/updateConnectStatusByClientId")
    public R updateConnectStatusByClientId(@RequestBody MqttsDevice mqttsDevice) {
        return R.ok(mqttsDeviceService.updateConnectStatusByClientId(mqttsDevice.getConnectStatus(), mqttsDevice.getClientId()));
    }

    /**
     * 认证接口
     */
    @GetMapping("/findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType")
    public R<MqttsDevice> findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(@RequestParam(value = "clientId", required = true) String clientId,
                                                                                   @RequestParam(value = "userName", required = true) String userName,
                                                                                   @RequestParam(value = "password", required = true) String password,
                                                                                   @RequestParam(value = "deviceStatus", required = true) String deviceStatus,
                                                                                   @RequestParam(value = "protocolType", required = true) String protocolType) {
        return R.ok(mqttsDeviceService.findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(clientId, userName, password, deviceStatus, protocolType));
    }
}
