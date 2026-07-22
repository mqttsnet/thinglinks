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

/**
 * <p>
 * 表单查询方法返回值VO
 * 规则条件动作详情
 * </p>
 *
 * @author mqttsnet
 * @date 2023-07-19 23:24:37
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "RuleConditionActionDetailsResultVO", description = "规则条件动作详情VO")
public class RuleConditionActionDetailsResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 规则条件ID
     */
    @Schema(description = "规则条件ID")
    private Long ruleConditionId;
    /**
     * 执行动作
     */
    @Schema(description = "执行动作")
    private Integer actionType;
    /**
     * 动作内容
     */
    @Schema(description = "动作内容")
    private String actionContent;
    /**
     * 描述
     */
    @Schema(description = "描述")
    private String remark;


}
