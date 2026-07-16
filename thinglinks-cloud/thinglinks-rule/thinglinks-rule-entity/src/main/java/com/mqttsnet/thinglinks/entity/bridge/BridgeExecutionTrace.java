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
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

/**
 * <p>
 * 实体类：桥接执行 trace 主表（链路回放用）
 * 对应表 rule_bridge_execution_trace
 * </p>
 *
 * <p>日志类表，无加密字段。trace_id 全局唯一，与设备 publish 日志串联。
 * 写入由 {@code BridgeTraceEventListener} 异步事件驱动（{@code @Async("ruleBridgeLogExecutor")}），
 * 主链路 0 DB I/O 阻塞。</p>
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
@TableName("rule_bridge_execution_trace")
public class BridgeExecutionTrace extends Entity<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 全链路追踪ID（贯穿 mqs → RocketMQ → rule）。
     */
    @TableField(value = "trace_id", condition = EQUAL)
    private String traceId;

    /**
     * 关联桥接规则 ID（出站必填；入站为订阅源拉取时为空）。
     */
    @TableField(value = "bridge_rule_id", condition = EQUAL)
    private Long bridgeRuleId;

    /**
     * 桥接方向：10-出站 / 20-入站。
     */
    @TableField(value = "direction", condition = EQUAL)
    private String direction;

    /**
     * 触发来源：DEVICE_DATA / SUBSCRIPTION / TEST_SINK / REPLAY。
     */
    @TableField(value = "trigger_source", condition = EQUAL)
    private String triggerSource;

    /**
     * 产品标识（出站时来自设备事件）。
     */
    @TableField(value = "product_identification", condition = EQUAL)
    private String productIdentification;

    /**
     * 设备标识（出站时来自设备事件）。
     */
    @TableField(value = "device_identification", condition = EQUAL)
    private String deviceIdentification;

    /**
     * 事件类型（PUBLISH/CONNECT/CLOSE/...）。
     */
    @TableField(value = "action_type", condition = EQUAL)
    private String actionType;

    /**
     * 设备事件 topic。
     */
    @TableField(value = "topic", condition = LIKE)
    private String topic;

    /**
     * 关联数据源 ID（出站=目标 sink；入站=来源 source）。
     */
    @TableField(value = "data_source_id", condition = EQUAL)
    private Long dataSourceId;

    /**
     * 关联订阅源 ID（仅入站）。
     */
    @TableField(value = "subscription_source_id", condition = EQUAL)
    private Long subscriptionSourceId;

    /**
     * 整体状态:00-成功 / 01-失败 / 02-部分成功 / 03-死信。
     */
    @TableField(value = "status", condition = EQUAL)
    private String status;

    /**
     * 执行的步骤总数。
     */
    @TableField(value = "step_count")
    private Integer stepCount;

    /**
     * 总耗时毫秒（开始到结束）。
     */
    @TableField(value = "total_latency_ms")
    private Integer totalLatencyMs;

    /**
     * 执行开始时间（毫秒精度）。
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 执行结束时间（毫秒精度）。
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 源消息摘要（envelope 前 1KB；便于排查 + 死信回放）。
     */
    @TableField(value = "source_payload_summary")
    private String sourcePayloadSummary;

    /**
     * 结果摘要（成功的 sink / 失败原因等一句话）。
     */
    @TableField(value = "result_summary")
    private String resultSummary;

    /**
     * 失败时的错误信息。
     */
    @TableField(value = "error_msg")
    private String errorMsg;

    /**
     * 扩展参数。
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
