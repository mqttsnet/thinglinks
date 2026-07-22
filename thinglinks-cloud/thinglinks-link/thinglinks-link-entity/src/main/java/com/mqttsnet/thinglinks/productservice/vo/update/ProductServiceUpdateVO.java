package com.mqttsnet.thinglinks.productservice.vo.update;

import com.mqttsnet.basic.base.entity.SuperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import com.mqttsnet.thinglinks.product.constant.ThingModelCodeRule;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 表单修改方法VO
 * 产品模型服务表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "ProductServiceUpdateVO", description = "产品模型服务表")
public class ProductServiceUpdateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "服务id")
    @NotNull(message = "请填写服务id", groups = SuperEntity.Update.class)
    private Long id;

    /**
     * 产品ID
     */
    @Schema(description = "产品ID")
    private Long productId;
    /**
     * 服务编码(用作底层数据表标识)。
     */
    @Schema(description = "服务编码(用作数据表标识):小写字母开头,仅含小写字母、数字、下划线,长度2-50")
    @NotEmpty(message = "请填写服务编码")
    @Pattern(regexp = ThingModelCodeRule.PATTERN, message = ThingModelCodeRule.PATTERN_MSG)
    private String serviceCode;
    /**
     * 服务名称
     */
    @Schema(description = "服务名称")
    @Size(max = 255, message = "服务名称长度不能超过{max}")
    private String serviceName;
    /**
     * 服务类型
     */
    @Schema(description = "服务类型")
    @Size(max = 255, message = "服务类型长度不能超过{max}")
    private String serviceType;
    /**
     * 状态(字典值：0启用  1停用)
     */
    @Schema(description = "状态(字典值：0启用  1停用)")
    @NotNull(message = "请填写状态(字典值：0启用  1停用)")
    private Integer serviceStatus;
    /**
     * 服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。
     */
    @Schema(description = "服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。")
    @Size(max = 255, message = "服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。长度不能超过{max}")
    private String description;
    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;


}
