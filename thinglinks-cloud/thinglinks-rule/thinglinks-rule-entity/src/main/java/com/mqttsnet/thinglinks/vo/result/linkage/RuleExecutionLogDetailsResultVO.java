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

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 表单查询方法返回值VO
 * 规则执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:41:26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "RuleExecutionLogDetailsResultVO", description = "规则执行日志")
public class RuleExecutionLogDetailsResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 规则标识
     */
    @Schema(description = "规则标识")
    private String ruleIdentification;
    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String ruleName;
    /**
     * 规则执行状态：0-未执行，1-执行中，2-已完成
     */
    @Schema(description = "规则执行状态：0-未执行，1-执行中，2-已完成")
    private Integer status;
    /**
     * 规则执行开始时间
     */
    @Schema(description = "规则执行开始时间")
    private LocalDateTime startTime;
    /**
     * 规则执行结束时间
     */
    @Schema(description = "规则执行结束时间")
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

    @Schema(description = "步骤总数")
    private Integer stepCount;

    @Schema(description = "总耗时毫秒")
    private Long totalLatencyMs;

    @Schema(description = "触发来源")
    private String triggerSource;

    @Schema(description = "结果摘要")
    private String resultSummary;

    @Schema(description = "执行步骤")
    private List<RuleExecutionLogStepResultVO> steps;

    @Schema(description = "规则条件执行日志列表")
    private List<RuleConditionExecutionLogDetailsResultVO> conditionExecutionLogDetailsResultVOList;

    @Schema(description = "规则动作执行日志列表")
    private List<RuleActionExecutionLogDetailsResultVO> actionExecutionLogDetailsResultVOList;


}
