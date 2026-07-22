package com.mqttsnet.thinglinks.mqs.bus.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 协议总线统计接口,租户隔离(走 ContextUtil 自动注入).
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/mqs/bus/stats")
@RequiredArgsConstructor
@Tag(name = "协议总线-统计", description = "租户级实时指标查询")
@Slf4j
public class DeviceBusStatsController {

    private final BusStatsService busStatsService;

    @Operation(summary = "当日 dispatcher 总事件分布")
    @GetMapping("/today/dispatch")
    public R<Map<String, Long>> todayDispatch() {
        return R.success(busStatsService.queryTodayDispatchTotal());
    }

    @Operation(summary = "当日 stage 执行分布")
    @GetMapping("/today/stage")
    public R<Map<String, Long>> todayStage() {
        return R.success(busStatsService.queryTodayStageExecutions());
    }

    @Operation(summary = "当日 relay 投递分布")
    @GetMapping("/today/relay")
    public R<Map<String, Long>> todayRelay() {
        return R.success(busStatsService.queryTodayRelaySend());
    }

    @Operation(summary = "查询指定日期的指定维度",
        description = "dispatch_total / stage_executions / no_route / canonicalize_fail / relay_send")
    @GetMapping("/dimension")
    public R<Map<String, Long>> queryDimension(
        @Parameter(description = "日期(yyyyMMdd),为空取今天") @RequestParam(required = false) String date,
        @Parameter(description = "维度名(必填)", required = true) @RequestParam String dimension) {
        if (StrUtil.isBlank(dimension)) {
            return R.fail("dimension 不能为空");
        }
        return R.success(busStatsService.queryDimension(date, dimension));
    }

    @Operation(summary = "当日全维度汇总 ── 前端 dashboard 一次拉齐")
    @GetMapping("/today/summary")
    public R<Map<String, Object>> todaySummary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dispatch", busStatsService.queryTodayDispatchTotal());
        result.put("stage", busStatsService.queryTodayStageExecutions());
        result.put("relay", busStatsService.queryTodayRelaySend());
        result.put("noRoute", busStatsService.queryDimension(null, BusStatsService.DIM_NO_ROUTE));
        result.put("canonicalizeFail", busStatsService.queryDimension(null, BusStatsService.DIM_CANONICALIZE_FAIL));
        return R.success(result);
    }

    @Operation(summary = "聚合统计 ── 按 dispatch_total 的 status 字段汇总")
    @GetMapping("/today/health")
    public R<Map<String, Long>> todayHealth() {
        Map<String, Long> dispatch = busStatsService.queryTodayDispatchTotal();
        Map<String, Long> result = new LinkedHashMap<>();
        long total = 0L, success = 0L, failed = 0L, dropped = 0L, noRoute = 0L;
        for (Map.Entry<String, Long> e : dispatch.entrySet()) {
            long count = e.getValue() == null ? 0L : e.getValue();
            total += count;
            String[] parts = e.getKey().split(":");
            String status = parts.length >= 4 ? parts[3] : "_";
            switch (status) {
                case "00", "SUCCESS" -> success += count;
                case "01", "FAILED" -> failed += count;
                case "04", "DROPPED" -> dropped += count;
                case "05", "NO_ROUTE" -> noRoute += count;
                default -> { /* 其它状态(如 PARTIAL)不单独计 */ }
            }
        }
        result.put("total", total);
        result.put("success", success);
        result.put("failed", failed);
        result.put("dropped", dropped);
        result.put("noRoute", noRoute);
        return R.success(result);
    }
}
