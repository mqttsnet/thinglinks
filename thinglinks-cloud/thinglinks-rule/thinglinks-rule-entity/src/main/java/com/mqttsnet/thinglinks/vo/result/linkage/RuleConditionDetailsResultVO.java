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

import java.util.List;

/**
 * <p>
 * 表单查询方法返回值VO
 * 规则条件详情
 * </p>
 *
 * @author mqttsnet
 * @date 2023-07-19 23:36:30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "RuleConditionDetailsResultVO", description = "规则条件详情VO")
public class RuleConditionDetailsResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 规则
     */
    @Schema(description = "规则")
    private Long ruleId;
    /**
     * 条件编码
     */
    @Schema(description = "条件编码")
    private String conditionIdentification;
    /**
     * 条件类型
     */
    @Schema(description = "条件类型")
    private Integer conditionType;
    /**
     * 条件内容
     */
    @Schema(description = "条件内容")
    private String conditionScheme;
    /**
     * 启用状态
     */
    @Schema(description = "启用状态")
    private Integer status;
    /**
     * 防抖状态
     */
    @Schema(description = "防抖状态")
    private Integer antiShake;
    /**
     * 防抖策略
     */
    @Schema(description = "防抖策略")
    private String antiShakeScheme;
    /**
     * 描述
     */
    @Schema(description = "描述")
    private String remark;

    @Schema(description = "规则条件动作详情VO集合")
    private List<RuleConditionActionDetailsResultVO> conditionActionDetailsResultVOS;

}
