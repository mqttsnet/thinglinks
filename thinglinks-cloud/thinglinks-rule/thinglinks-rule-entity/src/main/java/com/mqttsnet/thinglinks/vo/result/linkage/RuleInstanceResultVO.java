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
 * 规则实例表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-07-05 23:04:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "RuleInstanceResultVO", description = "规则实例表")
public class RuleInstanceResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String appId;
    /**
     * 规则实例名称
     */
    @Schema(description = "规则实例名称")
    private String ruleName;
    /**
     * 流程ID， 规则实例类型为“规则编排”时，该项为对应的NedRed流程
     */
    @Schema(description = "流程ID， 规则实例类型为“规则编排”时，该项为对应的NedRed流程")
    private String flowId;
    /**
     * 流程数据
     */
    @Schema(description = "流程数据")
    private String flowData;
    /**
     * 规则实例类型(字典标识：RULE_INSTANCE_TYPE）
     */
    @Schema(description = "规则实例类型(字典标识：RULE_INSTANCE_TYPE）")
    private Integer type;

    /**
     * 实例地址
     */
    @Schema(description = "实例地址")
    private String instanceAddress;
    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


}
