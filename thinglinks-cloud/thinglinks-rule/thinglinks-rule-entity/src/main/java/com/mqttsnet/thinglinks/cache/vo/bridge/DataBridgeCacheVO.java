package com.mqttsnet.thinglinks.cache.vo.bridge;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <p>
 * 桥接规则缓存 VO ── 对齐 {@code DeviceCacheVO} 模式。
 * <p>
 * 字段保持与 {@code DataBridge} 实体的 getter 方法名完全一致,让 matcher / dispatcher
 * 热路径的 {@code rule.getXxx()} 调用与原 entity 兼容,无需大面积重构。
 * <p>
 * 不包含审计字段({@code remark / createdOrgId / createdBy / createdTime / updatedBy /
 * updatedTime / deleted}),减少缓存噪音。
 * </p>
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "DataBridgeCacheVO", description = "桥接规则缓存 VO")
public class DataBridgeCacheVO extends Entity<Long> implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "id")
    private Long id;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则业务唯一编码")
    private String ruleCode;

    @Schema(description = "桥接方向：10-出站(平台→第三方) / 20-入站(第三方→平台)")
    private String direction;

    @Schema(description = "关联数据源 FK→rule_data_source.id")
    private Long dataSourceId;

    @Schema(description = "匹配条件 JSON")
    private String matchConfigJson;

    @Schema(description = "动作配置 JSON(已解密)")
    private String actionConfigJson;

    @Schema(description = "规则级可靠性级别覆盖")
    private Integer qos;

    @Schema(description = "规则级 QPS 限流覆盖")
    private Integer rateLimitQps;

    @Schema(description = "规则级最大重试次数覆盖")
    private Integer retryMaxTimes;

    @Schema(description = "规则级初始退避时长覆盖(毫秒)")
    private Integer retryBackoffMs;

    @Schema(description = "规则级单次发送超时覆盖(毫秒)")
    private Integer timeoutMs;

    @Schema(description = "规则级死信数据源覆盖")
    private Long deadLetterDataSourceId;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "优先级(数字越小越先匹配)")
    private Integer priority;

    @Schema(description = "扩展参数")
    private String extendParams;
}
