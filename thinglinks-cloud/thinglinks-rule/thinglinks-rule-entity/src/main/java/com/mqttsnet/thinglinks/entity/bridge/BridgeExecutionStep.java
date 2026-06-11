package com.mqttsnet.thinglinks.entity.bridge;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;

/**
 * <p>
 * 实体类：桥接执行步骤明细
 * 对应表 rule_bridge_execution_step
 * </p>
 *
 * <p>日志类表，无加密字段。一次执行（trace）按 step_no 升序展开 N 个步骤，
 * 前端"链路回放"详情抽屉按 step_type 渲染不同的子卡片（INGEST / RULE_MATCH / TRANSFORM / SINK_SEND 等）。</p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName("rule_bridge_execution_step")
public class BridgeExecutionStep extends Entity<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联 trace（FK→rule_bridge_execution_trace.trace_id）。
     */
    @TableField(value = "trace_id", condition = EQUAL)
    private String traceId;

    /**
     * 关联桥接规则 ID（同 traceId 命中多条规则时区分 step 归属）。
     */
    @TableField(value = "bridge_rule_id", condition = EQUAL)
    private Long bridgeRuleId;

    /**
     * 步骤顺序号（从1起，前端按此排序）。
     */
    @TableField(value = "step_no")
    private Integer stepNo;

    /**
     * 类型枚举:INGEST / RULE_MATCH / RATE_LIMIT / TRANSFORM / SINK_SEND / DEAD_LETTER / INBOUND_FORWARD。
     */
    @TableField(value = "step_type", condition = EQUAL)
    private String stepType;

    /**
     * 步骤可读名称（中文，前端卡片标题用）。
     */
    @TableField(value = "step_name")
    private String stepName;

    /**
     * 状态:00-成功 / 01-失败 / 02-跳过。
     */
    @TableField(value = "status", condition = EQUAL)
    private String status;

    /**
     * 本步骤耗时（毫秒）。
     */
    @TableField(value = "latency_ms")
    private Integer latencyMs;

    /**
     * 输入摘要 JSON。
     */
    @TableField(value = "input_summary")
    private String inputSummary;

    /**
     * 输出摘要 JSON。
     */
    @TableField(value = "output_summary")
    private String outputSummary;

    /**
     * 失败错误（status=01 时填）。
     */
    @TableField(value = "error_msg")
    private String errorMsg;

    /**
     * 步骤开始时间（毫秒精度）。
     */
    @TableField(value = "started_at")
    private LocalDateTime startedAt;

    /**
     * 扩展参数（步骤特异协议数据 JSON）：
     * SINK_SEND 含 sinkType/partition/messageId；
     * RULE_MATCH 含命中条件细节；
     * RATE_LIMIT 含阈值/当前 QPS；
     * TRANSFORM 含 scriptId/scriptVersion 等。
     */
    @TableField(value = "extend_params")
    private String extendParams;

    /**
     * 备注。
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 创建人组织。
     */
    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;
}
