package com.mqttsnet.thinglinks.rule.controller;

import com.mqttsnet.thinglinks.common.core.annotation.NoRepeatSubmit;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanUtils;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.log.enums.BusinessType;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.rule.api.domain.ActionCommands;
import com.mqttsnet.thinglinks.rule.api.domain.model.ActionCommandsModel;
import com.mqttsnet.thinglinks.rule.service.ActionCommandsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/actionCommands")
@RestController
public class ActionCommandsController extends BaseController {


    @Resource
    private ActionCommandsService actionCommandsService;




    @PreAuthorize(hasPermi = "rule:rule:list")
    @GetMapping("/list")
    public TableDataInfo list(ActionCommands actionCommands) {
        List<ActionCommands> list = actionCommandsService.selectByActionCommandsSelective(actionCommands );
        return getDataTable(list);
    }

    /**
     * 批量新增执行动作命令
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "rule:actionCommands:add")
    @Log(title = "执行动作命令", businessType = BusinessType.INSERT)
    @PostMapping("/batchInsert")
    public AjaxResult batchInsert(@RequestBody List<ActionCommands> actionCommandsList) {
        return AjaxResult.success(actionCommandsService.batchInsert(actionCommandsList));
    }

    /**
     * 批量编辑执行动作命令
     */
    @NoRepeatSubmit
    @PreAuthorize(hasPermi = "rule:actionCommands:edit")
    @Log(title = "执行动作命令", businessType = BusinessType.UPDATE)
    @PutMapping("/batchEdit")
    public AjaxResult updateBatch(@RequestBody List<ActionCommands> actionCommandsList) {
        return toAjax(actionCommandsService.updateBatchSelective(actionCommandsList));
    }

    /**
     * 批量删除执行动作命令
     * @param ids
     * @return
     */
    @PreAuthorize(hasPermi = "link:actionCommands:remove")
    @Log(title = "执行动作命令", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(actionCommandsService.deleteBatchByIds(ids));
    }
}
