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

/**
 * <p>
 * 表单查询条件 VO
 * 桥接执行步骤明细
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
@Schema(title = "BridgeExecutionStepPageQuery", description = "桥接执行步骤明细 分页查询参数")
public class BridgeExecutionStepPageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联 trace ID")
    private String traceId;

    @Schema(description = "步骤类型")
    private String stepType;

    @Schema(description = "状态：00-成功 / 01-失败 / 02-跳过")
    private String status;
}
