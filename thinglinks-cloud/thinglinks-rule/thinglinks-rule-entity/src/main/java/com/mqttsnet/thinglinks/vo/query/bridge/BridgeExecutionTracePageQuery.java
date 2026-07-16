package com.mqttsnet.thinglinks.vo.query.bridge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 表单查询条件 VO
 * 桥接执行 trace（链路回放）
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "BridgeExecutionTracePageQuery", description = "桥接执行 trace 分页查询参数")
public class BridgeExecutionTracePageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "全链路追踪 ID（精确）")
    private String traceId;

    @Schema(description = "关联桥接规则 ID")
    private Long bridgeRuleId;

    @Schema(description = "桥接方向")
    private String direction;

    @Schema(description = "触发来源")
    private String triggerSource;

    @Schema(description = "产品标识")
    private String productIdentification;

    @Schema(description = "设备标识")
    private String deviceIdentification;

    @Schema(description = "事件类型")
    private String actionType;

    @Schema(description = "整体状态：00-成功 / 01-失败 / 02-部分成功 / 03-死信")
    private String status;

    @Schema(description = "开始时间-起（按 start_time 区间）")
    private LocalDateTime startTimeBegin;

    @Schema(description = "开始时间-止")
    private LocalDateTime startTimeEnd;
}
