package com.mqttsnet.thinglinks.manager.bridge;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionStep;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionStepPageQuery;

import java.util.List;

/**
 * <p>
 * 通用业务接口
 * 桥接执行步骤明细
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
public interface BridgeExecutionStepManager extends SuperManager<BridgeExecutionStep> {

    List<BridgeExecutionStep> getStepList(BridgeExecutionStepPageQuery query);

    /**
     * 按 traceId 取所有步骤（按 step_no 升序）── 详情抽屉用,可能包含多规则的混合数据
     */
    List<BridgeExecutionStep> getStepsByTraceId(String traceId);

    /**
     * 按 (traceId, ruleId) 精确取该规则的步骤序列 ── 多规则场景必用
     */
    List<BridgeExecutionStep> getStepsByTraceIdAndRuleId(String traceId, Long ruleId);
}
