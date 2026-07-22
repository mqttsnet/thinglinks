package com.mqttsnet.thinglinks.vo.result.bridge;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>
 * 表单查询返回值 VO
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "DataBridgeResultVO", description = "数据桥接-规则")
public class DataBridgeResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则业务唯一编码")
    private String ruleCode;

    @Schema(description = "桥接方向")
    private String direction;

    @Schema(description = "关联数据源 ID")
    private Long dataSourceId;

    @Schema(description = "关联数据源业务编码(service join 反查;列表/卡片/详情友好展示用)")
    private String dataSourceCode;

    @Schema(description = "关联数据源名称(service join 反查;列表/卡片/详情友好展示用)")
    private String dataSourceName;

    @Schema(description = "匹配条件 JSON")
    private String matchConfigJson;

    @Schema(description = "动作配置 JSON（已解密）")
    private String actionConfigJson;

    @Schema(description = "规则级 QoS 覆盖")
    private Integer qos;

    @Schema(description = "规则级 QPS 限流覆盖")
    private Integer rateLimitQps;

    @Schema(description = "规则级最大重试次数覆盖")
    private Integer retryMaxTimes;

    @Schema(description = "规则级初始退避时长覆盖")
    private Integer retryBackoffMs;

    @Schema(description = "规则级单次发送超时覆盖")
    private Integer timeoutMs;

    @Schema(description = "规则级死信数据源覆盖")
    private Long deadLetterDataSourceId;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "扩展参数")
    private String extendParams;

    @Schema(description = "备注")
    private String remark;
}
