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
 * 产品模型设备服务命令表
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
@ApiModel(value = "ProductCommandParamVO", description = "产品模型设备服务命令参数VO")
public class ProductCommandParamVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;


    /**
     * 服务ID
     */
    @ApiModelProperty(value = "服务ID")
    @NotNull(message = "请填写服务ID")
    private Long serviceId;
    /**
     * 指示命令的编码，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。支持英文大小写、数字及下划线，长度[2,50]。
     */
    @ApiModelProperty(value = "指示命令的编码，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。支持英文大小写、数字及下划线，长度[2,50]。")
    @NotEmpty(message = "请填写指示命令的编码，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。支持英文大小写、数字及下划线，长度[2,50]。")
    @Size(max = 255, message = "指示命令的编码，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。支持英文大小写、数字及下划线，长度[2,50]。长度不能超过{max}")
    private String commandCode;
    /**
     * 指示命令名称
     */
    @ApiModelProperty(value = "指示命令名称")
    @Size(max = 255, message = "指示命令名称长度不能超过{max}")
    private String commandName;
    /**
     * 命令描述。
     */
    @ApiModelProperty(value = "命令描述")
    @Size(max = 255, message = "命令描述。长度不能超过{max}")
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

    @ApiModelProperty(value = "产品请求服务命令属性")
    private List<ProductCommandRequestParamVO> requests;

    @ApiModelProperty(value = "产品响应服务命令属性")
    private List<ProductCommandResponseParamVO> responses;

}
