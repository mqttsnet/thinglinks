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
 * 数据桥接-规则
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
@Schema(title = "DataBridgePageQuery", description = "数据桥接-规则 分页查询参数")
public class DataBridgePageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "规则名称（模糊查询）")
    private String ruleName;

    @Schema(description = "规则业务唯一编码")
    private String ruleCode;

    @Schema(description = "桥接方向")
    private String direction;

    @Schema(description = "关联数据源 ID")
    private Long dataSourceId;

    @Schema(description = "是否启用")
    private Boolean enable;
}
