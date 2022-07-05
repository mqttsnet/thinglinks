package com.mqttsnet.thinglinks.link.controller.protocol;

import com.mqttsnet.thinglinks.common.core.utils.SecurityUtils;
import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.protocol.Protocol;
import com.mqttsnet.thinglinks.link.service.protocol.ProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 协议管理Controller
 *
 * @author thinglinks
 * @date 2022-07-04
 */
@RestController
@RequestMapping("/protocol")
public class ProtocolController extends BaseController
{
    @Autowired
    private ProtocolService protocolService;

    /**
     * 查询协议管理列表
     */
    @PreAuthorize(hasPermi = "link:protocol:list")
    @GetMapping("/list")
    public TableDataInfo list(Protocol protocol)
    {
        startPage();
        List<Protocol> list = protocolService.selectProtocolList(protocol);
        return getDataTable(list);
    }

    /**
     * 导出协议管理列表
     */
    @PreAuthorize(hasPermi = "link:protocol:export")
    @Log(title = "协议管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Protocol protocol) throws IOException
    {
        List<Protocol> list = protocolService.selectProtocolList(protocol);
        ExcelUtil<Protocol> util = new ExcelUtil<Protocol>(Protocol.class);
        util.exportExcel(response, list, "协议管理数据");
    }

    /**
     * 获取协议管理详细信息
     */
    @PreAuthorize(hasPermi = "link:protocol:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(protocolService.selectProtocolById(id));
    }

    /**
     * 新增协议管理
     */
    @PreAuthorize(hasPermi = "link:protocol:add")
    @Log(title = "协议管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Protocol protocol)
    {
        protocol.setCreateBy(SecurityUtils.getUsername());
        return toAjax(protocolService.insertProtocol(protocol));
    }

    /**
     * 修改协议管理
     */
    @PreAuthorize(hasPermi = "link:protocol:edit")
    @Log(title = "协议管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Protocol protocol)
    {
        protocol.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(protocolService.updateProtocol(protocol));
    }

    /**
     * 删除协议管理
     */
    @PreAuthorize(hasPermi = "link:protocol:remove")
    @Log(title = "协议管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(protocolService.deleteProtocolByIds(ids));
    }

    /**
     * 启用协议管理
     * @param ids
     * @return
     */
    @PreAuthorize(hasPermi = "link:protocol:enable")
    @Log(title = "协议管理", businessType = BusinessType.GRANT)
    @DeleteMapping("/{ids}")
    public AjaxResult enable(@PathVariable Long[] ids)
    {
        return toAjax(protocolService.enable(ids));
    }
    /**
     * 停用协议管理
     * @param ids
     * @return
     */
    @PreAuthorize(hasPermi = "link:protocol:disable")
    @Log(title = "协议管理", businessType = BusinessType.GRANT)
    @DeleteMapping("/{ids}")
    public AjaxResult disable(@PathVariable Long[] ids)
    {
        return toAjax(protocolService.disable(ids));
    }
}
