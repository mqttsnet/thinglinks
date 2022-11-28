package com.mqttsnet.thinglinks.rule.controller;

import com.mqttsnet.thinglinks.common.core.annotation.NoRepeatSubmit;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.rule.api.domain.RuleConditions;
import com.mqttsnet.thinglinks.rule.service.RuleConditionsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/ruleConditions")
public class RuleConditionsController extends BaseController {




    @Resource
    private RuleConditionsService ruleConditionsService;
    /**
     * 新增产品管理
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "rule:ruleConditions:add")
    @Log(title = "规则条件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody List<RuleConditions> ruleConditions) {
        return toAjax(ruleConditionsService.batchInsert(ruleConditions));
    }

    /**
     * 新增规则条件
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "rule:ruleConditions:edit")
    @Log(title = "规则条件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody List<RuleConditions> ruleConditions) {
        return toAjax(ruleConditionsService.updateBatch(ruleConditions));
    }

    /**
     * 修改规则条件
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "rule:ruleConditions:edit")
    @Log(title = "规则条件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult delete(@RequestBody List<RuleConditions> ruleConditions) {
        return toAjax(ruleConditionsService.updateBatch(ruleConditions));
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @PreAuthorize(hasPermi = "link:ruleConditions:remove")
    @Log(title = "规则条件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(ruleConditionsService.deleteBatchByIds(ids));
    }
}
