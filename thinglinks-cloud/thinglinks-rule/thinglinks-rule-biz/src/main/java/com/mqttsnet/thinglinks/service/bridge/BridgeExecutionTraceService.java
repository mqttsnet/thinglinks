package com.mqttsnet.thinglinks.service.bridge;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionTrace;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionTracePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.BridgeExecutionTraceResultVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 业务接口
 * 桥接执行 trace（链路回放用）
 * </p>
 *
 * <p>日志类表，无 Save/Update 业务接口；仅暴露查询 + 死信回放。</p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
public interface BridgeExecutionTraceService extends SuperService<Long, BridgeExecutionTrace> {

    /**
     * 查询 trace VO 列表（列表页/搜索结果用）。
     *
     * @param query 查询参数（含规则ID/状态/时间区间/设备等过滤）
     * @return {@link List<BridgeExecutionTraceResultVO>} trace VO 列表，不含 steps 子集合
     */
    List<BridgeExecutionTraceResultVO> getTraceResultVOList(BridgeExecutionTracePageQuery query);

    /**
     * 查询 trace 详情（含 steps 子集合，前端"链路回放"详情抽屉用）。
     * <p>同 traceId 命中多规则时返回最新一条;精确查询用 {@link #getTraceDetail(String, Long)}。
     *
     * @param traceId 全链路追踪 ID
     * @return {@link BridgeExecutionTraceResultVO} 含 steps List 的完整详情
     */
    BridgeExecutionTraceResultVO getTraceDetail(String traceId);

    /**
     * 按 (traceId, ruleId) 精确查询 trace 详情(多规则场景必用)。
     *
     * @param traceId 全链路追踪 ID
     * @param ruleId  桥接规则 ID;null 则退化为 {@link #getTraceDetail(String)}
     */
    BridgeExecutionTraceResultVO getTraceDetail(String traceId, Long ruleId);

    /**
     * 死信重放。
     * <p>从 trace 取 source_payload_summary + bridge_rule_id，重新走 SinkDispatcher 完整链路。
     * 触发新的 trace 记录（trigger_source=REPLAY）。</p>
     *
     * @param traceId 原 trace ID
     * @return 新 trace ID（重放产生的新执行）
     */
    String replay(String traceId);

    /**
     * 删除 createdTime 早于 cutoff 的历史 trace(定时清理用)。
     *
     * @param cutoff 截止时间;早于该时间的全部物理删除
     * @return 实际删除是否生效(boolean,具体行数依赖 mybatis-plus 实现)
     */
    boolean removeBefore(LocalDateTime cutoff);
}
