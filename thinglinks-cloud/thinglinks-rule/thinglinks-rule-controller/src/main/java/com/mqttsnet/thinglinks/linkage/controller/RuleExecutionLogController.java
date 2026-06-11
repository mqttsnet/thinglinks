package com.mqttsnet.thinglinks.linkage.controller;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.entity.linkage.RuleExecutionLog;
import com.mqttsnet.thinglinks.service.linkage.RuleExecutionLogService;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogResultVO;
import com.mqttsnet.thinglinks.vo.save.linkage.RuleExecutionLogSaveVO;
import com.mqttsnet.thinglinks.vo.update.linkage.RuleExecutionLogUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * 规则执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:41:26
 * @create [2024-12-02 18:41:26] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/ruleExecutionLog")
@Tag(name = "规则执行日志")
public class RuleExecutionLogController extends SuperController<RuleExecutionLogService, Long, RuleExecutionLog, RuleExecutionLogSaveVO,
        RuleExecutionLogUpdateVO, RuleExecutionLogPageQuery, RuleExecutionLogResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    /**
     * 获取规则执行日志详情
     *
     * @param id 规则执行日志ID
     * @return 规则执行日志详情
     */
    @Operation(summary = "获取规则执行日志详情", description = "Fetches the detailed information of a rule execution log by its ID")
    @Parameters({@Parameter(name = "id", description = "规则执行日志ID", required = true)})
    @GetMapping("/getRuleExecutionLogDetails/{id}")
    public R<RuleExecutionLogDetailsResultVO> getRuleExecutionLogDetails(@PathVariable("id") Long id) {
        log.info("getRuleExecutionLogDetails id: {}", id);
        RuleExecutionLogDetailsResultVO result = superService.getRuleExecutionLogDetails(id);
        echoService.action(result);
        return R.success(result);
    }


}


