package com.mqttsnet.thinglinks.productcommand.vo.save;

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
@Schema(title = "ProductCommandSaveVO", description = "产品模型设备服务命令表")
public class ProductCommandSaveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务ID
     */
    @Schema(description = "服务ID")
    @NotNull(message = "请填写服务ID")
    private Long serviceId;
    /**
     * 指示命令的编码,如门磁的 lock 命令、摄像头的 video_record 命令,命令名与参数共同构成一个完整的命令。
     */
    @Schema(description = "命令编码(如 lock、video_record):小写字母开头,仅含小写字母、数字、下划线,长度2-50")
    @NotEmpty(message = "请填写命令编码")
    @Pattern(regexp = ThingModelCodeRule.PATTERN, message = ThingModelCodeRule.PATTERN_MSG)
    private String commandCode;
    /**
     * 指示命令名称
     */
    @Schema(description = "指示命令名称")
    @Size(max = 255, message = "指示命令名称长度不能超过{max}")
    private String commandName;
    /**
     * 命令描述。
     */
    @Schema(description = "命令描述。")
    @Size(max = 255, message = "命令描述。长度不能超过{max}")
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
