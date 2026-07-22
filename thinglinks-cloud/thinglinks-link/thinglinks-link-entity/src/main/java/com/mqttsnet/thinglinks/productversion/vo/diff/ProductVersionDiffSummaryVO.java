package com.mqttsnet.thinglinks.productversion.vo.diff;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品版本差异摘要(列表行展示用,轻量计数)。
 *
 * @author mqttsnet
 * @see ProductVersionDiffVO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "ProductVersionDiffSummaryVO", description = "版本差异摘要")
public class ProductVersionDiffSummaryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "产品基础信息变更字段数")
    @Builder.Default
    private Integer productInfoChanged = 0;

    @Schema(description = "服务新增数")
    @Builder.Default
    private Integer serviceAdded = 0;

    @Schema(description = "服务删除数")
    @Builder.Default
    private Integer serviceRemoved = 0;

    @Schema(description = "服务修改数")
    @Builder.Default
    private Integer serviceModified = 0;

    @Schema(description = "属性新增数")
    @Builder.Default
    private Integer propertyAdded = 0;

    @Schema(description = "属性删除数")
    @Builder.Default
    private Integer propertyRemoved = 0;

    @Schema(description = "属性修改数")
    @Builder.Default
    private Integer propertyModified = 0;

    @Schema(description = "命令新增数")
    @Builder.Default
    private Integer commandAdded = 0;

    @Schema(description = "命令删除数")
    @Builder.Default
    private Integer commandRemoved = 0;

    @Schema(description = "命令修改数")
    @Builder.Default
    private Integer commandModified = 0;
}
