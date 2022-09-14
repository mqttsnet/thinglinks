package com.mqttsnet.thinglinks.link.controller.device;

import com.mqttsnet.thinglinks.common.core.annotation.NoRepeatSubmit;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatus;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.model.DeviceParams;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备管理Controller
 *
 * @author thinglinks
 * @date 2021-10-22
 */
@RestController
@RequestMapping("/device")
@Slf4j
public class DeviceController extends BaseController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ProductService productService;

    /**
     * 查询设备管理列表
     */
    @PreAuthorize(hasPermi = "link:device:list")
    @GetMapping("/list")
    public R<Map<String, Object>> list(Device device) {
        startPage();
        final Map<String, Object> results = new HashMap<>();
        List<Device> list = deviceService.selectDeviceList(device);
        //查询设备数据
        results.put("device", getDataTable(list));
        //统计设备在线数量
        results.put("onlineCount", deviceService.countDistinctClientIdByConnectStatus(DeviceConnectStatus.ONLINE.getValue()));
        //统计设备离线数量
        results.put("offlineCount", deviceService.countDistinctClientIdByConnectStatus(DeviceConnectStatus.OFFLINE.getValue()));
        //统计设备初始化数量
        results.put("initCount", deviceService.countDistinctClientIdByConnectStatus(DeviceConnectStatus.INIT.getValue()));
        return R.ok(results);
    }

    /**
     * 根据产品标识查询所属的设备标识
     */
    @GetMapping("/selectByProductIdentification")
    public R<?> selectByProductIdentification(String productIdentification) {
        return R.ok(deviceService.selectByProductIdentification(productIdentification));
    }

    /**
     * 导出设备管理列表
     */
    @PreAuthorize(hasPermi = "link:device:export")
    @Log(title = "设备管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Device device) throws IOException {
        List<Device> list = deviceService.selectDeviceList(device);
        ExcelUtil<Device> util = new ExcelUtil<Device>(Device.class);
        util.exportExcel(response, list, "设备管理数据");
    }

    /**
     * 获取设备管理详细信息
     */
    @PreAuthorize(hasPermi = "link:device:query")
    @GetMapping(value = {"/", "/{id}"})
    public AjaxResult getInfo(@PathVariable(value = "id", required = false) Long id) {
        AjaxResult ajax = AjaxResult.success();
        if (StringUtils.isNotNull(id)) {
            ajax.put(AjaxResult.DATA_TAG, deviceService.selectDeviceModelById(id));
            ajax.put("products", productService.selectProductList(new Product()));
        } else {
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
    public AjaxResult add(@RequestBody DeviceParams deviceParams) {
        try {
            return toAjax(deviceService.insertDevice(deviceParams));
        } catch (Exception e) {
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
    public AjaxResult edit(@RequestBody DeviceParams deviceParams) {
        try {
            return toAjax(deviceService.updateDevice(deviceParams));
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 删除设备管理
     */
    @PreAuthorize(hasPermi = "link:device:remove")
    @Log(title = "设备管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
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
     * 校验clientId是否存在
     *
     * @param clientId
     * @return
     */
    @Log(title = "设备管理", businessType = BusinessType.OTHER)
    @GetMapping(value = "/validationFindOneByClientId/{clientId}")
    public AjaxResult validationFindOneByClientId(@PathVariable("clientId") String clientId) {
        if (StringUtils.isEmpty(clientId)) {
            return AjaxResult.error("clientId不可为空");
        }
        Device findOneByClientId = deviceService.findOneByClientId(clientId);
        if (StringUtils.isNull(findOneByClientId)) {
            return AjaxResult.success("clientId可用");
        }
        return AjaxResult.error("clientId已存在");
    }

    /**
     * 校验设备标识是否存在
     *
     * @param deviceIdentification
     * @return
     */
    @Log(title = "设备管理", businessType = BusinessType.OTHER)
    @GetMapping(value = "/validationFindOneByDeviceIdentification/{deviceIdentification}")
    public AjaxResult validationFindOneByDeviceIdentification(@PathVariable("deviceIdentification") String deviceIdentification) {
        if (StringUtils.isEmpty(deviceIdentification)) {
            return AjaxResult.error("设备标识不可为空");
        }
        Device findOneByDeviceIdentification = deviceService.findOneByDeviceIdentification(deviceIdentification);
        if (StringUtils.isNull(findOneByDeviceIdentification)) {
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
    public AjaxResult disconnect(@PathVariable Long[] ids) {
        final Boolean disconnect = deviceService.disconnect(ids);
        return disconnect ? AjaxResult.success("操作成功") : AjaxResult.error("操作失败");
    }

    /**
     * 客户端身份认证
     *
     * @param params
     * @return
     */
    @PostMapping("/clientAuthentication")
    public ResponseEntity<AjaxResult> clientAuthentication(@RequestBody Map<String, Object> params) {
        final Object clientIdentifier = params.get("clientIdentifier");
        final Object username = params.get("username");
        final Object password = params.get("password");
        final Object deviceStatus = params.get("deviceStatus");
        final Object protocolType = params.get("protocolType");
        Boolean certificationStatus = deviceService.clientAuthentication(clientIdentifier.toString(), username.toString(), password.toString(), deviceStatus.toString(), protocolType.toString());
        log.info("{} 协议设备正在进行身份认证,客户端ID:{},用户名:{},密码:{},认证结果:{}", protocolType, clientIdentifier, username, password, certificationStatus ? "成功" : "失败");
        return certificationStatus ? ResponseEntity.ok().body(AjaxResult.success("认证成功")) : ResponseEntity.status(403).body(AjaxResult.error("认证失败"));
    }

    /**
     * 根据客户端标识获取设备信息
     * @param clientId
     * @return
     */
    @GetMapping("/findOneByClientId")
    public R<Device> findOneByClientId(String clientId) {
        return R.ok(deviceService.findOneByClientId(clientId));
    }


}
