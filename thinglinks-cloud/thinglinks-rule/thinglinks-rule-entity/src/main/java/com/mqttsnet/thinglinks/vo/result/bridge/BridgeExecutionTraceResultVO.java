package com.mqttsnet.thinglinks.vo.result.bridge;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 表单查询返回值 VO
 * 桥接执行 trace 主表（链路回放用）
 * </p>
 *
 * <p>详情接口返回时含 {@link #steps} 子集合（步骤列表，前端详情抽屉用）。</p>
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
@Schema(title = "BridgeExecutionTraceResultVO", description = "桥接执行 trace 返回值")
public class BridgeExecutionTraceResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "全链路追踪ID")
    private String traceId;

    @Schema(description = "关联桥接规则 ID")
    private Long bridgeRuleId;

    @Schema(description = "桥接方向：10-出站 / 20-入站")
    private String direction;

    @Schema(description = "触发来源：DEVICE_DATA / SUBSCRIPTION / TEST_SINK / REPLAY")
    private String triggerSource;

    @Schema(description = "产品标识")
    private String productIdentification;

    @Schema(description = "设备标识")
    private String deviceIdentification;

    @Schema(description = "事件类型")
    private String actionType;

    @Schema(description = "设备事件 topic")
    private String topic;

    @Schema(description = "关联数据源 ID")
    private Long dataSourceId;

    @Schema(description = "关联订阅源 ID（仅入站）")
    private Long subscriptionSourceId;

    @Schema(description = "整体状态：00-成功 / 01-失败 / 02-部分成功 / 03-死信")
    private String status;

    @Schema(description = "执行的步骤总数")
    private Integer stepCount;

    @Schema(description = "总耗时毫秒")
    private Integer totalLatencyMs;

    @Schema(description = "执行开始时间（毫秒精度）")
    private LocalDateTime startTime;

    @Schema(description = "执行结束时间（毫秒精度）")
    private LocalDateTime endTime;

    @Schema(description = "源消息摘要")
    private String sourcePayloadSummary;

    @Schema(description = "结果摘要")
    private String resultSummary;

    @Schema(description = "失败时的错误信息")
    private String errorMsg;

    @Schema(description = "扩展参数")
    private String extendParams;

    @Schema(description = "备注")
    private String remark;

    /**
     * 步骤列表（仅详情接口返回；分页列表接口返回 null）。
     * <p>前端"链路回放"详情抽屉按 step_no 升序渲染卡片。
     */
    @Schema(description = "步骤列表（详情接口）")
    private List<BridgeExecutionStepResultVO> steps;
}
