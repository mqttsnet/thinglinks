package com.mqttsnet.thinglinks.linkage.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.entity.linkage.Rule;
import com.mqttsnet.thinglinks.service.linkage.RuleService;
import com.mqttsnet.thinglinks.vo.query.linkage.RulePageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleResultVO;
import com.mqttsnet.thinglinks.vo.save.linkage.RuleSaveVO;
import com.mqttsnet.thinglinks.vo.update.linkage.RuleUpdateVO;
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
 * 规则信息
 * </p>
 *
 * @author mqttsnet
 * @date 2023-07-19 23:20:14
 * @create [2023-07-19 23:20:14] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/rule")
@Tag(name = "规则信息")
public class RuleController extends SuperController<RuleService, Long, Rule, RuleSaveVO,
        RuleUpdateVO, RulePageQuery, RuleResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<Rule> handlerWrapper(Rule model, PageParams<RulePageQuery> params) {
        QueryWrap<Rule> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("rule");
        return queryWrap;
    }

    /**
     * 新增 规则信息表
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存规则信息")
    @PostMapping("/saveRule")
    @WebLog(value = "保存规则信息", request = false)
    public R saveRule(@RequestBody RuleSaveVO saveVO) {
        return R.success(superService.saveRule(saveVO));
    }


    /**
     * 修改 规则信息表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改规则信息")
    @PutMapping("/updateRule")
    @WebLog(value = "修改规则信息", request = false)
    public R updateRule(@RequestBody RuleUpdateVO updateVO) {
        return R.success(superService.updateRule(updateVO));
    }

    /**
     * 删除规则信息
     *
     * @param id 规则信息ID
     * @return 删除结果
     */
    @Operation(summary = "删除规则信息", description = "根据规则信息ID删除规则信息")
    @Parameters({@Parameter(name = "id", description = "规则信息ID", required = true),})
    @DeleteMapping("/deleteRule/{id}")
    @WebLog(value = "删除规则信息", request = false)
    public R<Boolean> deleteRule(@PathVariable("id") Long id) {
        log.info("deleteRule id:{}", id);
        return R.success(superService.deleteRule(id));
    }

    /**
     * 批量删除规则信息
     *
     * @param ids 规则信息ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除规则信息", description = "根据规则信息ID列表批量删除规则信息")
    @Parameters({@Parameter(name = "ids", description = "规则信息ID列表", required = true),})
    @DeleteMapping("/deleteRules")
    @WebLog(value = "批量删除规则信息", request = false)
    public R<Boolean> deleteRules(@RequestBody List<Long> ids) {
        log.info("deleteRules ids:{}", ids);
        boolean allDeleted = ids.stream().distinct().allMatch(id -> superService.deleteRule(id));
        return R.success(allDeleted);
    }


    /**
     * 修改 规则状态
     *
     * @param id     对象ID
     * @param status 新状态值
     * @return 实体
     */
    @Operation(summary = "修改规则状态", description = "修改规则状态 ")
    @Parameters({
            @Parameter(name = "id", description = "对象ID", required = true),
            @Parameter(name = "status", description = "新状态值", required = true),
    })
    @PutMapping("/updateRuleStatus/{id}")
    @WebLog(value = "修改规则状态", request = false)
    public R<Boolean> updateRuleStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        log.info("updateRuleStatus id:{},status:{}", id, status);
        return R.success(superService.updateRuleStatus(id, status));
    }


    /**
     * 获取规则详情
     *
     * @param id The Rule ID.
     * @return Rule Details.
     */
    @Operation(summary = "获取规则详情", description = "Fetches the detailed information of a rule by its ID")
    @Parameters({@Parameter(name = "id", description = "Rule ID", required = true),})
    @GetMapping("/getRuleDetails/{id}")
    public R<RuleDetailsResultVO> getRuleDetails(@PathVariable("id") Long id) {
        log.info("getRuleDetails id: {}", id);
        RuleDetailsResultVO result = superService.getRuleDetails(id);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * Fetches the detailed information of a rule by its unique identification string.
     * <p>
     * This method retrieves the details of a specific rule identified by its unique rule identification string.
     * It is used to obtain detailed information about the rule, including its properties, configurations, and current status.
     * </p>
     *
     * @param ruleIdentification The unique identification string of the rule.
     * @return {@link RuleDetailsResultVO} containing the details of the rule.
     */
    @Operation(summary = "获取规则详情", description = "Fetches the detailed information of a rule by its unique identification string.")
    @Parameters({@Parameter(name = "ruleIdentification", description = "Rule Identification", required = true),})
    @GetMapping("/getRuleDetailsByIdentification/{ruleIdentification}")
    public R<RuleDetailsResultVO> getRuleDetailsByIdentification(@PathVariable("ruleIdentification") String ruleIdentification) {
        log.info("getRuleDetailsByIdentification: {}", ruleIdentification);
        RuleDetailsResultVO result = superService.getRuleDetailsByIdentification(ruleIdentification);
        echoService.action(result);
        return R.success(result);
    }


}


