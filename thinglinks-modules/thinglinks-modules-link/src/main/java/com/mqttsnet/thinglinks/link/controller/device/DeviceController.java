package com.mqttsnet.thinglinks.link.controller.device;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.mqttsnet.thinglinks.common.core.annotation.NoRepeatSubmit;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;

/**
 * 设备管理Controller
 *
 * @author thinglinks
 * @date 2021-10-22
 */
@RestController
@RequestMapping("/device")
public class DeviceController extends BaseController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ProductService productService;
    @Autowired
    private TokenService tokenService;

    /**
     * 查询设备管理列表
     */
    @PreAuthorize(hasPermi = "link:device:list")
    @GetMapping("/list")
    public TableDataInfo list(Device device)
    {
        startPage();
        List<Device> list = deviceService.selectDeviceList(device);
        return getDataTable(list);
    }

    /**
     * 导出设备管理列表
     */
    @PreAuthorize(hasPermi = "link:device:export")
    @Log(title = "设备管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Device device) throws IOException
    {
        List<Device> list = deviceService.selectDeviceList(device);
        ExcelUtil<Device> util = new ExcelUtil<Device>(Device.class);
        util.exportExcel(response, list, "设备管理数据");
    }

    /**
     * 获取设备管理详细信息
     */
    @PreAuthorize(hasPermi = "link:device:query")
    @GetMapping(value = { "/", "/{id}" })
    public AjaxResult getInfo(@PathVariable(value = "id", required = false) Long id)
    {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        AjaxResult ajax = AjaxResult.success();
        if (StringUtils.isNotNull(id))
        {
            ajax.put(AjaxResult.DATA_TAG,deviceService.selectDeviceById(id));
            ajax.put("products", productService.selectProductList(new Product()));
        }else {
            ajax.put("products", productService.selectProductList(new Product()));
        }
        return ajax;
    }

    /**
     * 新增设备管理
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "link:device:add")
    @Log(title = "设备管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Device device)
    {
        try {
            return toAjax(deviceService.insertDevice(device));
        }catch (Exception e){
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 修改设备管理
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "link:device:edit")
    @Log(title = "设备管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Device device)
    {
        return toAjax(deviceService.updateDevice(device));
    }

    /**
     * 删除设备管理
     */
    @PreAuthorize(hasPermi = "link:device:remove")
    @Log(title = "设备管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(deviceService.deleteDeviceByIds(ids));
    }

    /**
     * 更新设备在线状态
     */
    @PutMapping("/updateConnectStatusByClientId")
    public R updateConnectStatusByClientId(@RequestBody Device device) {
        return R.ok(deviceService.updateConnectStatusByClientId(device.getConnectStatus(), device.getClientId()));
    }

    /**
     * 设备认证接口
     */
    @GetMapping("/findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType")
    public R<Device> findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(@RequestParam(value = "clientId", required = true) String clientId,
                                                                                           @RequestParam(value = "userName", required = true) String userName,
                                                                                           @RequestParam(value = "password", required = true) String password,
                                                                                           @RequestParam(value = "deviceStatus", required = true) String deviceStatus,
                                                                                           @RequestParam(value = "protocolType", required = true) String protocolType) {
        return R.ok(deviceService.findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(clientId, userName, password, deviceStatus, protocolType));
    }

    /**
     *校验clientId是否存在
     * @param clientId
     * @return
     */
    @Log(title = "设备管理", businessType = BusinessType.OTHER)
    @GetMapping(value = "/validationfindOneByClientId/{clientId}")
    public AjaxResult validationfindOneByClientId(@PathVariable("clientId") String clientId)
    {
        Device findOneByClientId = deviceService.findOneByClientId(clientId);
        if (StringUtils.isNull(findOneByClientId)){
            return AjaxResult.success("clientId可用");
        }
        return AjaxResult.error("clientId已存在");
    }

    /**
     *校验设备标识是否存在
     * @param deviceIdentification
     * @return
     */
    @Log(title = "设备管理", businessType = BusinessType.OTHER)
    @GetMapping(value = "/validationFindOneByDeviceIdentification/{deviceIdentification}")
    public AjaxResult validationFindOneByDeviceIdentification(@PathVariable("deviceIdentification") String deviceIdentification)
    {
        Device findOneByDeviceIdentification = deviceService.findOneByDeviceIdentification(deviceIdentification);
        if (StringUtils.isNull(findOneByDeviceIdentification)){
            return AjaxResult.success("设备标识可用");
        }
        return AjaxResult.error("设备标识已存在");
    }


    /**
     * 设备断开连接接口
     */
    @PreAuthorize(hasPermi = "link:device:disconnect")
    @Log(title = "设备管理", businessType = BusinessType.OTHER)
    @PostMapping("/disconnect/{ids}")
    public AjaxResult disconnect(@PathVariable Long[] ids)
    {
        deviceService.disconnect(ids);
        return AjaxResult.success("操作成功");
    }

}
