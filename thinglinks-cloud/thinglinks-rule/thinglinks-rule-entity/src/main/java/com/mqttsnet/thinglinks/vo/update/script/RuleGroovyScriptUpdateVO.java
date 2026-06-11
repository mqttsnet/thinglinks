package com.mqttsnet.thinglinks.vo.update.script;

import com.mqttsnet.basic.base.entity.SuperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
 * 表单修改方法VO
 * 规则脚本表
 * </p>
 *
 * @author mqttsnet
 * @date 2025-03-24 09:54:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "RuleGroovyScriptUpdateVO", description = "规则脚本表")
public class RuleGroovyScriptUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @NotNull(message = "请填写主键", groups = SuperEntity.Update.class)
    private Long id;

    /**
     * 脚本名称
     */
    @Schema(description = "脚本名称")
    @NotEmpty(message = "请填写脚本名称")
    @Size(max = 255, message = "脚本名称长度不能超过{max}")
    private String name;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    @NotEmpty(message = "请填写应用ID")
    @Size(max = 128, message = "应用ID长度不能超过{max}")
    private String appId;
    /**
     * 脚本类型
     */
    @Schema(description = "脚本类型")
    @NotEmpty(message = "请填写脚本类型")
    @Size(max = 128, message = "脚本类型长度不能超过{max}")
    private String scriptType;
    /**
     * 渠道编码
     */
    @Schema(description = "渠道编码")
    @NotEmpty(message = "请填写渠道编码")
    @Size(max = 128, message = "渠道编码长度不能超过{max}")
    private String channelCode;
    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    @NotEmpty(message = "请填写产品标识")
    @Size(max = 128, message = "产品标识长度不能超过{max}")
    private String productIdentification;
    /**
     * 主题模式
     */
    @Schema(description = "主题模式")
    @NotEmpty(message = "请填写主题模式")
    @Size(max = 100, message = "主题模式长度不能超过{max}")
    private String topicPattern;
    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    @NotNull(message = "请填写是否启用")
    private Boolean enable;
    /**
     * 脚本内容
     */
    @Schema(description = "脚本内容")
    @NotEmpty(message = "请填写脚本内容")
    @Size(max = 2147483647, message = "脚本内容长度不能超过{max}")
    private String scriptContent;
    /**
     * 扩展信息
     */
    @Schema(description = "扩展信息")
    @Size(max = 2147483647, message = "扩展信息长度不能超过{max}")
    private String extendParams;
    /**
     * 版本号
     */
    @Schema(description = "版本号")
    @NotEmpty(message = "请填写版本号")
    @Size(max = 100, message = "版本号长度不能超过{max}")
    private String objectVersion;
    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过{max}")
    private String remark;


}
