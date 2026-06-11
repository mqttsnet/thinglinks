package com.mqttsnet.thinglinks.vo.result.script;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 规则脚本执行统计 ── 详情页展示用(生命周期累计)。
 *
 * @author mqttsnet
 */
@Data
@Schema(title = "RuleScriptExecStatVO", description = "规则脚本执行统计")
public class RuleScriptExecStatVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总执行次数")
    private long total;

    @Schema(description = "成功次数")
    private long success;

    @Schema(description = "失败次数")
    private long fail;
}
