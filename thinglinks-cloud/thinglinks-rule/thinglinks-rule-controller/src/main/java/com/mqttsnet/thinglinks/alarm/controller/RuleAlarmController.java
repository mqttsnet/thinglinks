package com.mqttsnet.thinglinks.alarm.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.entity.alarm.RuleAlarm;
import com.mqttsnet.thinglinks.service.alarm.RuleAlarmService;
import com.mqttsnet.thinglinks.vo.query.alarm.RuleAlarmPageQuery;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmResultVO;
import com.mqttsnet.thinglinks.vo.save.alarm.RuleAlarmSaveVO;
import com.mqttsnet.thinglinks.vo.update.alarm.RuleAlarmUpdateVO;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * 告警规则表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-09-09 21:14:37
 * @create [2023-09-09 21:14:37] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/ruleAlarm")
@Tag(name = "告警规则")
public class RuleAlarmController extends SuperController<RuleAlarmService, Long, RuleAlarm, RuleAlarmSaveVO,
        RuleAlarmUpdateVO, RuleAlarmPageQuery, RuleAlarmResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<RuleAlarm> handlerWrapper(RuleAlarm model, PageParams<RuleAlarmPageQuery> params) {
        QueryWrap<RuleAlarm> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("rule_alarm");
        return queryWrap;
    }

    /**
     * 新增 告警规则
     *
     * @param ruleAlarmSaveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存告警规则", description = "保存告警规则")
    @PostMapping("/saveRuleAlarm")
    @WebLog(value = "保存告警规则", request = false)
    public R<RuleAlarmSaveVO> saveRuleAlarm(@RequestBody RuleAlarmSaveVO ruleAlarmSaveVO) {
        return R.success(superService.saveRuleAlarm(ruleAlarmSaveVO));
    }

    /**
     * 修改 告警规则
     *
     * @param ruleAlarmUpdateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改告警规则", description = "修改告警规则")
    @PutMapping("/updateRuleAlarm")
    @WebLog(value = "修改告警规则", request = false)
    public R<RuleAlarmUpdateVO> updateRuleAlarm(@RequestBody RuleAlarmUpdateVO ruleAlarmUpdateVO) {
        return R.success(superService.updateRuleAlarm(ruleAlarmUpdateVO));
    }

    /**
     * 删除告警规则
     *
     * @param id 告警规则ID
     * @return 删除结果
     */
    @Operation(summary = "删除告警规则", description = "根据告警规则ID删除告警规则")
    @Parameters({
            @Parameter(name = "id", description = "告警规则ID", required = true)
    })
    @DeleteMapping("/deleteRuleAlarm/{id}")
    @WebLog(value = "删除告警规则", request = false)
    public R<Boolean> deleteRuleAlarm(@PathVariable("id") Long id) {
        log.info("deleteAlarmRule id:{}", id);
        return R.success(superService.deleteRuleAlarm(id));
    }

    /**
     * 获取告警规则详情
     *
     * @param id 告警规则ID
     * @return 告警规则详情
     */
    @Operation(summary = "获取告警规则详情", description = "根据告警规则ID获取告警规则详情")
    @Parameters({
            @Parameter(name = "id", description = "告警规则ID", required = true)
    })
    @GetMapping("/getRuleAlarmDetails/{id}")
    public R<RuleAlarmDetailsResultVO> getRuleAlarmDetails(@PathVariable("id") Long id) {
        log.info("getAlarmRuleDetails id:{}", id);
        RuleAlarmDetailsResultVO result = superService.getRuleAlarmDetails(id);
        echoService.action(result);
        return R.success(result);
    }


}


