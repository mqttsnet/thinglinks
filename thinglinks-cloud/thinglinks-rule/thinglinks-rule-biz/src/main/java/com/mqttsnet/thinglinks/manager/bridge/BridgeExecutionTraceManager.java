package com.mqttsnet.thinglinks.manager.bridge;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionTrace;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionTracePageQuery;

import java.util.List;

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
     * 同 traceId 命中多规则时返回最新一条(orderBy id desc limit 1);精确查询用 {@link #getByTraceIdAndRuleId}。
     */
    BridgeExecutionTrace getByTraceId(String traceId);

    /**
     * 按 (traceId, ruleId) 精确查询 ── trace 表 uk_trace_rule 联合唯一保证唯一性。
     */
    BridgeExecutionTrace getByTraceIdAndRuleId(String traceId, Long ruleId);
}
