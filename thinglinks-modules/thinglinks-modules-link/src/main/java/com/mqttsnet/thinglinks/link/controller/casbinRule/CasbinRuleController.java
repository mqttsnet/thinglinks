package com.mqttsnet.thinglinks.link.controller.casbinRule;

import com.mqttsnet.thinglinks.common.core.utils.poi.ExcelUtil;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.CasbinRule;
import com.mqttsnet.thinglinks.link.service.casbinRule.CasbinRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * CAS规则管理Controller
 *
 * @author thinglinks
 * @date 2022-06-16
 */
@RestController
@RequestMapping("/casbinRule")
@Slf4j
public class CasbinRuleController extends BaseController
{
    @Autowired
    private CasbinRuleService casbinRuleService;

    /**
     * 查询CAS规则管理列表
     */
    @PreAuthorize(hasPermi = "link:casbinRule:list")
    @GetMapping("/list")
    public TableDataInfo list(CasbinRule casbinRule)
    {
        startPage();
        List<CasbinRule> list = casbinRuleService.selectCasbinRuleList(casbinRule);
        return getDataTable(list);
    }

    /**
     * 导出CAS规则管理列表
     */
    @PreAuthorize(hasPermi = "link:casbinRule:export")
    @Log(title = "CAS规则管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CasbinRule casbinRule) throws IOException
    {
        List<CasbinRule> list = casbinRuleService.selectCasbinRuleList(casbinRule);
        ExcelUtil<CasbinRule> util = new ExcelUtil<CasbinRule>(CasbinRule.class);
        util.exportExcel(response, list, "CAS规则管理数据");
    }

    /**
     * 获取CAS规则管理详细信息
     */
    @PreAuthorize(hasPermi = "link:casbinRule:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(casbinRuleService.selectCasbinRuleById(id));
    }

    /**
     * 新增CAS规则管理
     */
    @PreAuthorize(hasPermi = "link:casbinRule:add")
    @Log(title = "CAS规则管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CasbinRule casbinRule)
    {
        return toAjax(casbinRuleService.insertCasbinRule(casbinRule));
    }

    /**
     * 修改CAS规则管理
     */
    @PreAuthorize(hasPermi = "link:casbinRule:edit")
    @Log(title = "CAS规则管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CasbinRule casbinRule)
    {
        return toAjax(casbinRuleService.updateCasbinRule(casbinRule));
    }

    /**
     * 删除CAS规则管理
     */
    @PreAuthorize(hasPermi = "link:casbinRule:remove")
    @Log(title = "CAS规则管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(casbinRuleService.deleteCasbinRuleByIds(ids));
    }
}