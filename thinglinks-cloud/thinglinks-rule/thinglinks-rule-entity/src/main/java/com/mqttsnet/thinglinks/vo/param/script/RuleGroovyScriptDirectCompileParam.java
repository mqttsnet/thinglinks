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
 * 规则脚本直接编译参数 实体
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
@Schema(title = "RuleGroovyScriptDirectCompileParam", description = "规则脚本直接编译参数")
public class RuleGroovyScriptDirectCompileParam {


    /**
     * 执行参数
     */
    @Schema(description = "执行参数")
    @NotEmpty(message = "请填写执行参数")
    @Size(max = 2147483647, message = "执行参数长度不能超过{max}")
    private String executeParams;

    /**
     * 脚本内容
     */
    @Schema(description = "脚本内容")
    @NotEmpty(message = "请填写脚本内容")
    @Size(max = 2147483647, message = "脚本内容长度不能超过{max}")
    private String scriptContent;

    /**
     * 脚本唯一键(scriptType:channelCode:productIdentification:topicPattern)── 可选,执行统计按脚本维度计数用。
     * 运行时由 mqs 转换器拼好传入;纯调试可不传(不计统计)。
     */
    @Schema(description = "脚本唯一键(执行统计用,可选)")
    private String scriptUniqueKey;

}
