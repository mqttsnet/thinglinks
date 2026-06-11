package com.mqttsnet.thinglinks.vo.param.script;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * GroovyScript 查询实体
 *
 * @author mqttsnet 2025/03/18 12:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "RuleGroovyScriptParam", description = "规则脚本参数")
public class RuleGroovyScriptParam {

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
     * 脚本内容
     */
    @Schema(description = "脚本内容")
    @NotEmpty(message = "请填写脚本内容")
    @Size(max = 2147483647, message = "脚本内容长度不能超过{max}")
    private String scriptContent;

}
