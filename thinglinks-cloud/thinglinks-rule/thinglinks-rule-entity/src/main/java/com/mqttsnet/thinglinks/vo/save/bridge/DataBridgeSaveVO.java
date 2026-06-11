package com.mqttsnet.thinglinks.vo.save.bridge;

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
 * 表单保存方法 VO
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
@Schema(title = "DataBridgeSaveVO", description = "数据桥接-规则")
public class DataBridgeSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    @Size(max = 128, message = "应用ID长度不能超过{max}")
    private String appId;

    @Schema(description = "规则名称")
    @NotEmpty(message = "请填写规则名称")
    @Size(max = 255, message = "规则名称长度不能超过{max}")
    private String ruleName;

    @Schema(description = "规则业务唯一编码（snowflake，不传则后端自动生成）")
    @Size(max = 128, message = "规则编码长度不能超过{max}")
    private String ruleCode;

    @Schema(description = "桥接方向：10-出站 / 20-入站")
    @NotEmpty(message = "请选择桥接方向")
    private String direction;

    @Schema(description = "关联数据源 ID")
    @NotNull(message = "请选择数据源")
    private Long dataSourceId;

    @Schema(description = "匹配条件 JSON（structured：productIds/actionTypes/topicPatterns/...）")
    @NotEmpty(message = "请填写匹配条件")
    private String matchConfigJson;

    @Schema(description = "动作配置 JSON（EncryptTypeHandler 加密落盘）")
    @NotEmpty(message = "请填写动作配置")
    private String actionConfigJson;

    @Schema(description = "规则级 QoS 覆盖（NULL=用数据源默认）")
    private Integer qos;

    @Schema(description = "规则级 QPS 限流覆盖")
    private Integer rateLimitQps;

    @Schema(description = "规则级最大重试次数覆盖")
    private Integer retryMaxTimes;

    @Schema(description = "规则级初始退避时长覆盖（毫秒）")
    private Integer retryBackoffMs;

    @Schema(description = "规则级单次发送超时覆盖（毫秒）")
    private Integer timeoutMs;

    @Schema(description = "规则级死信数据源覆盖")
    private Long deadLetterDataSourceId;

    @Schema(description = "优先级（数字越小越先匹配）")
    private Integer priority;

    @Schema(description = "扩展参数")
    @Size(max = 2048, message = "扩展参数长度不能超过{max}")
    private String extendParams;

    @Schema(description = "备注")
    @Size(max = 512, message = "备注长度不能超过{max}")
    private String remark;
}
