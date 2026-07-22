package com.mqttsnet.thinglinks.manager.bridge;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionTrace;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionTracePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.BridgeExecutionTraceStatsResultVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 通用业务接口
 * 桥接执行 trace（链路回放用）
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
public interface BridgeExecutionTraceManager extends SuperManager<BridgeExecutionTrace> {

    List<BridgeExecutionTrace> getTraceList(BridgeExecutionTracePageQuery query);

    /**
     * 按查询条件聚合桥接执行日志统计。
     *
     * @param query 查询条件
     * @return 统计结果
     */
    BridgeExecutionTraceStatsResultVO getTraceStats(BridgeExecutionTracePageQuery query);

    /**
     * 按查询条件获取桥接执行日志时间范围。
     *
     * @param query 查询条件
     * @return 时间范围
     */
    Map<String, Object> getTraceTimeBounds(BridgeExecutionTracePageQuery query);

    /**
     * 按查询条件聚合桥接执行日志趋势。
     *
     * @param query       查询条件
     * @param granularity 时间粒度
     * @return 趋势点
     */
    List<BridgeExecutionTraceStatsResultVO.TimelinePoint> getTraceTimeline(BridgeExecutionTracePageQuery query,
                                                                           String granularity);

    /**
     * 按查询条件聚合触发量 Top 规则。
     *
     * @param query 查询条件
     * @param limit 返回数量
     * @return Top 规则
     */
    List<BridgeExecutionTraceStatsResultVO.TopRule> getTraceTopRules(BridgeExecutionTracePageQuery query,
                                                                     int limit);

    /**
     * 同 traceId 命中多规则时返回最新一条(orderBy id desc limit 1);精确查询用 {@link #getByTraceIdAndRuleId}。
     */
    BridgeExecutionTrace getByTraceId(String traceId);

    /**
     * 按 (traceId, ruleId) 精确查询；trace 表通过 uk_trace_rule_key 归一可空规则 ID 后保证唯一性。
     */
    BridgeExecutionTrace getByTraceIdAndRuleId(String traceId, Long ruleId);
}
