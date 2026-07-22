package com.mqttsnet.thinglinks.service.bridge;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionStep;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionStepPageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.BridgeExecutionStepResultVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 业务接口
 * 桥接执行步骤明细
 * </p>
 *
 * <p>日志类表，仅暴露查询接口（一般通过 trace 详情接口附带返回，单独接口主要给监控用）。</p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
public interface BridgeExecutionStepService extends SuperService<Long, BridgeExecutionStep> {

    /**
     * 查询步骤 VO 列表（监控告警系统单独按状态查异常步骤用）。
     *
     * @param query 查询参数（按 traceId / stepType / status 过滤）
     * @return {@link List<BridgeExecutionStepResultVO>} 步骤 VO 列表
     */
    List<BridgeExecutionStepResultVO> getStepResultVOList(BridgeExecutionStepPageQuery query);

    /**
     * 删除 startedAt 早于 cutoff 的历史 step(定时清理用)。
     *
     * @param cutoff 截止时间;早于该时间的全部物理删除
     * @return boolean 删除是否生效
     */
    boolean removeBefore(LocalDateTime cutoff);

    /**
     * 按 traceId 拉所有步骤(按 stepNo 升序);同 traceId 命中多规则时返回混合数据,精确查询用 {@link #getStepsByTraceIdAndRuleId}。
     */
    List<BridgeExecutionStep> getStepsByTraceId(String traceId);

    /**
     * 按 (traceId, ruleId) 精确查 step ── 多规则场景前端详情抽屉必用,确保只返回当前规则的步骤序列。
     */
    List<BridgeExecutionStep> getStepsByTraceIdAndRuleId(String traceId, Long ruleId);
}
