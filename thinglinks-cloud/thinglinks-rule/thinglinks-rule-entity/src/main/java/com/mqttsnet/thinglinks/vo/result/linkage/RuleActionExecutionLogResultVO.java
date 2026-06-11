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
 * 规则动作执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:54:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "RuleActionExecutionLogResultVO", description = "规则动作执行日志表")
public class RuleActionExecutionLogResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 规则执行日志ID（外键）
     */
    @Schema(description = "规则执行日志ID（外键）")
    private Long ruleExecutionId;
    /**
     * 动作类型：0-命令下发，1-触发告警，2-数据转发
     */
    @Schema(description = "动作类型：0-命令下发，1-触发告警，2-数据转发")
    private Integer actionType;
    /**
     * 动作内容
     */
    @Schema(description = "动作内容")
    private String actionContent;
    /**
     * 动作是否执行成功
     */
    @Schema(description = "动作是否执行成功")
    private Boolean result;
    /**
     * 动作开始执行时间
     */
    @Schema(description = "动作开始执行时间")
    private LocalDateTime startTime;
    /**
     * 动作结束执行时间
     */
    @Schema(description = "动作结束执行时间")
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
