package com.mqttsnet.thinglinks.vo.query.linkage;

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
 * 表单查询条件VO
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
@EqualsAndHashCode
@Builder
@Schema(title = "RuleExecutionLogPageQuery", description = "规则执行日志表")
public class RuleExecutionLogPageQuery implements Serializable {

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
     * 规则执行开始时间-起
     */
    @Schema(description = "规则执行开始时间-起")
    private LocalDateTime startTimeBegin;
    /**
     * 规则执行开始时间-止
     */
    @Schema(description = "规则执行开始时间-止")
    private LocalDateTime startTimeEnd;
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


}
