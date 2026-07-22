package com.mqttsnet.thinglinks.linkage.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.entity.linkage.RuleInstance;
import com.mqttsnet.thinglinks.service.linkage.RuleInstanceService;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleInstancePageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleInstanceResultVO;
import com.mqttsnet.thinglinks.vo.save.linkage.RuleInstanceSaveVO;
import com.mqttsnet.thinglinks.vo.update.linkage.RuleInstanceFlowUpdateVO;
import com.mqttsnet.thinglinks.vo.update.linkage.RuleInstanceUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * 规则实例表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-07-05 23:04:02
 * @create [2023-07-05 23:04:02] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/ruleInstance")
@Tag(name = "规则实例")
public class RuleInstanceController extends SuperController<RuleInstanceService, Long, RuleInstance, RuleInstanceSaveVO,
        RuleInstanceUpdateVO, RuleInstancePageQuery, RuleInstanceResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    @Override
    public QueryWrap<RuleInstance> handlerWrapper(RuleInstance model, PageParams<RuleInstancePageQuery> params) {
        QueryWrap<RuleInstance> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("rule_instance");
        return queryWrap;
    }

    /**
     * 新增 规则实例表
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存规则实例")
    @PostMapping("/saveRuleInstance")
    @WebLog(value = "保存规则实例", request = false)
    public R saveRuleInstance(@RequestBody RuleInstanceSaveVO saveVO) {
        return R.success(superService.saveRuleInstance(saveVO));
    }


    /**
     * 修改 规则实例表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改规则实例")
    @PutMapping("/updateRuleInstance")
    @WebLog(value = "修改规则实例", request = false)
    public R updateRuleInstance(@RequestBody RuleInstanceUpdateVO updateVO) {
        return R.success(superService.updateRuleInstance(updateVO));
    }

    @Operation(summary = "根据流程ID更新流程数据")
    @PutMapping("/updateRuleInstanceFlowData")
    @WebLog(value = "修改规则实例", request = false)
    public R updateRuleInstanceFlowData(@RequestBody RuleInstanceFlowUpdateVO updateVO) {
        return R.success(superService.updateRuleInstanceFlowData(updateVO));
    }


    /**
     * 删除规则实例
     *
     * @param id 规则实例ID
     * @return 删除结果
     */
    @Operation(summary = "删除规则实例", description = "根据规则实例ID删除规则实例")
    @Parameter(description = "规则实例ID", required = true)
    @DeleteMapping("/deleteRuleInstance/{id}")
    @WebLog(value = "删除规则实例", request = false)
    public R<Boolean> deleteRuleInstance(@PathVariable("id") Long id) {
        log.info("deleteRuleInstance id:{}", id);
        return R.success(superService.deleteRuleInstance(id));
    }

    /**
     * 批量删除规则实例信息
     *
     * @param ids 规则实例信息ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除规则实例信息", description = "根据规则实例信息ID列表批量删除规则实例信息")
    @Parameters({@Parameter(name = "ids", description = "规则实例信息ID列表", required = true),})
    @DeleteMapping("/deleteRuleInstances")
    @WebLog(value = "批量删除规则实例信息", request = false)
    public R<Boolean> deleteRuleInstances(@RequestBody List<Long> ids) {
        log.info("deleteRuleInstances ids:{}", ids);
        boolean allDeleted = ids.stream().distinct().allMatch(id -> superService.deleteRuleInstance(id));
        return R.success(allDeleted);
    }

    /**
     * 修改 规则实例状态
     *
     * @param id     对象ID
     * @param status 新状态值
     * @return 实体
     */
    @Operation(summary = "修改规则实例状态", description = "修改规则实例状态 ")
    @Parameters({
            @Parameter(name = "id", description = "对象ID", required = true),
            @Parameter(name = "status", description = "新状态值（0:未启用、1:启用）", required = true, example = "0,1")
    })
    @PutMapping("/updateRuleInstanceStatus/{id}")
    @WebLog(value = "修改规则实例状态", request = false)
    public R<Boolean> updateRuleInstanceStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        log.info("updateRuleInstanceStatus id:{},status:{}", id, status);
        return R.success(superService.updateRuleInstanceStatus(id, status));
    }

    /**
     * Retrieves detailed information of a rule instance by its flow ID.
     * <p>
     * This endpoint fetches detailed data about a rule instance using its unique flow ID.
     * It aims to provide extensive details such as configurations, properties, and the status of the rule instance.
     * </p>
     *
     * @param flowId The unique flow ID of the rule instance.
     * @return {@link RuleInstanceResultVO} containing the detailed information of the rule instance.
     */
    @Operation(summary = "根据流程ID获取规则实例详情", description = "Fetches detailed information of a rule instance by its flow ID.")
    @Parameter(description = "Flow ID", required = true)
    @GetMapping("/detailsByFlowId/{flowId}")
    public R<RuleInstanceResultVO> getDetailsByFlowId(
            @PathVariable("flowId") String flowId) {
        log.info("Fetching details for flow ID: {}", flowId);
        RuleInstanceResultVO result = superService.getDetailsByFlowId(flowId);
        echoService.action(result);
        return R.success(result);
    }

}


