package net.mqtts.link.controller.device;

import net.mqtts.common.core.domain.R;
import net.mqtts.common.core.web.controller.BaseController;
import net.mqtts.common.core.web.domain.AjaxResult;
import net.mqtts.common.core.web.page.TableDataInfo;
import net.mqtts.common.log.annotation.Log;
import net.mqtts.common.log.enums.BusinessType;
import net.mqtts.common.security.annotation.PreAuthorize;
import net.mqtts.link.api.domain.MqttsDeviceDatas;
import net.mqtts.link.service.device.MqttsDeviceDatasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备消息Controller
 *
 * @author shisen
 * @date 2021-11-20
 */
@RestController
@RequestMapping("/device/datas")
public class MqttsDeviceDatasController extends BaseController {
    @Autowired
    private MqttsDeviceDatasService mqttsDeviceDatasService;

    /**
     * 查询设备消息
     */
    @PreAuthorize(hasPermi = "link:device:datas:list")
    @GetMapping("/list")
    public TableDataInfo list(MqttsDeviceDatas mqttsDeviceDatas) {
        startPage();
        List<MqttsDeviceDatas> list = mqttsDeviceDatasService.selectMqttsDeviceDatasList(mqttsDeviceDatas);
        return getDataTable(list);
    }


    /**
     * 新增设备消息
     */
    @Log(title = "设备消息", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R add(@RequestBody MqttsDeviceDatas mqttsDeviceDatas) {
        return R.ok(mqttsDeviceDatasService.insertOrUpdate(mqttsDeviceDatas));
    }


    /**
     * 删除设备消息
     */
    @PreAuthorize(hasPermi = "link:device:datas:remove")
    @Log(title = "设备消息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(mqttsDeviceDatasService.deleteMqttsDeviceDatasByIds(ids));
    }
}
