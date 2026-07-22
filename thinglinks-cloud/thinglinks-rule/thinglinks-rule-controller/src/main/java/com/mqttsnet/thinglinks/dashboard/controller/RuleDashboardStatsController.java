package com.mqttsnet.thinglinks.dashboard.controller;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.service.dashboard.RuleDashboardStatsService;
import com.mqttsnet.thinglinks.vo.result.dashboard.RuleBridgeSummaryResultVO;
import com.mqttsnet.thinglinks.vo.result.dashboard.RuleDashboardSummaryResultVO;
import com.mqttsnet.thinglinks.vo.result.server.ServerResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * -----------------------------------------------------------------------------
 * File Name: RuleDashboardStatsController.java
 * -----------------------------------------------------------------------------
 * Description:
 * 数据统计
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2024-10-22 14:50
 */

@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/ruleDashboardStats")
@Tag(name = "Rule仪表盘数据统计")
public class RuleDashboardStatsController {

    private final RuleDashboardStatsService ruleDashboardStatsService;


    @Operation(summary = "获取仪表盘概要统计信息", description = "返回仪表板总览统计信息")
    @GetMapping("/assetSummary")
    public R<RuleDashboardSummaryResultVO> getDashboardAssetSummary() {
        log.info("Fetching dashboard assetSummary statistics");
        DataScopeHelper.startDataScope("rule");
        DataScopeHelper.startDataScope("rule_instance");
        DataScopeHelper.startDataScope("plugin_info");
        DataScopeHelper.startDataScope("rule_groovy_script");
        DataScopeHelper.startDataScope("rule_alarm");
        DataScopeHelper.startDataScope("rule_alarm_record");
        RuleDashboardSummaryResultVO summary = ruleDashboardStatsService.getDashboardAssetSummary();
        return R.success(summary);
    }


    @Operation(summary = "获取数据桥接统计信息", description = "返回数据源/桥接规则/订阅源总量与启用数 + 今日执行成功失败分布")
    @GetMapping("/bridgeSummary")
    public R<RuleBridgeSummaryResultVO> getBridgeSummary() {
        log.info("Fetching dashboard bridgeSummary statistics");
        DataScopeHelper.startDataScope("rule_data_source");
        DataScopeHelper.startDataScope("rule_data_bridge");
        DataScopeHelper.startDataScope("rule_subscription_source");
        DataScopeHelper.startDataScope("rule_bridge_execution_trace");
        RuleBridgeSummaryResultVO summary = ruleDashboardStatsService.getBridgeSummary();
        return R.success(summary);
    }


    @Operation(summary = "获取Rule服务监控", description = "获取Rule服务监控(CPU,内存等指标)")
    @GetMapping("/getRuleServerMonitor")
    public R<ServerResultVO> getRuleServerMonitor() {
        ServerResultVO serverResultVO = new ServerResultVO();
        serverResultVO.copyTo();
        return R.success(serverResultVO);
    }

}
