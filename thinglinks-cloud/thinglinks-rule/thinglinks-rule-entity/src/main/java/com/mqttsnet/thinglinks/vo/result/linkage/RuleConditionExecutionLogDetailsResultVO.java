package com.mqttsnet.thinglinks.vo.result.linkage;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 表单查询方法返回值VO
 * 规则条件执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:53:47
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "RuleConditionExecutionLogDetailsResultVO", description = "规则条件执行日志")
public class RuleConditionExecutionLogDetailsResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 规则执行日志ID
     */
    @Schema(description = "规则执行日志ID")
    private Long ruleExecutionId;
    /**
     * 条件唯一标识
     */
    @Schema(description = "条件唯一标识")
    private String conditionUuid;
    /**
     * 条件类型：0-设备属性触发，1-定时触发，2-设备动作触发等
     */
    @Schema(description = "条件类型：0-设备属性触发，1-定时触发，2-设备动作触发等")
    private Integer conditionType;
    /**
     * 条件是否成立
     */
    @Schema(description = "条件是否成立")
    private Boolean evaluationResult;
    /**
     * 条件评估开始时间
     */
    @Schema(description = "条件评估开始时间")
    private LocalDateTime startTime;
    /**
     * 条件评估结束时间
     */
    @Schema(description = "条件评估结束时间")
    private LocalDateTime endTime;
    /**
     * 描述
     */
    @Schema(description = "描述")
    private String remark;
    /**
     * 扩展参数（文本格式）
     */
    @Schema(description = "扩展参数（文本格式）")
    private String extendParams;


}
