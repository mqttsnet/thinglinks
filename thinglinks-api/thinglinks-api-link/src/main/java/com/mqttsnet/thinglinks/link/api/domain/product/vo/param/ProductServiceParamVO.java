package com.mqttsnet.thinglinks.link.api.domain.product.vo.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 表单保存方法VO
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
@ApiModel(value = "ProductServiceParamVO", description = "产品模型服务参数VO")
public class ProductServiceParamVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 产品ID
     */
    @ApiModelProperty(value = "产品ID")
    private Long productId;
    /**
     * 服务编码:支持英文大小写、数字、下划线和中划线
     */
    @ApiModelProperty(value = "服务编码:支持英文大小写、数字、下划线和中划线")
    @NotEmpty(message = "请填写服务编码:支持英文大小写、数字、下划线和中划线")
    @Size(max = 255, message = "服务编码:支持英文大小写、数字、下划线和中划线长度不能超过{max}")
    private String serviceCode;
    /**
     * 服务名称
     */
    @ApiModelProperty(value = "服务名称")
    @Size(max = 255, message = "服务名称长度不能超过{max}")
    private String serviceName;
    /**
     * 服务类型
     */
    @ApiModelProperty(value = "服务类型")
    @Size(max = 255, message = "服务类型长度不能超过{max}")
    private String serviceType;
    /**
     * 状态(字典值：0启用  1停用)
     */
    @ApiModelProperty(value = "状态(字典值：0启用  1停用)")
    @NotNull(message = "请填写状态(字典值：0启用  1停用)")
    private Integer serviceStatus;
    /**
     * 服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。
     */
    @ApiModelProperty(value = "服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。")
    @Size(max = 255, message = "服务的描述信息:文本描述，不影响实际功能，可配置为空字符串。长度不能超过{max}")
    private String description;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    @Size(max = 500, message = "备注长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @ApiModelProperty(value = "创建人组织")
    private Long createdOrgId;

    @ApiModelProperty(value = "产品服务命令")
    private List<ProductCommandParamVO> commands;

    @ApiModelProperty(value = "产品服务属性")
    private List<ProductPropertyParamVO> properties;

}
