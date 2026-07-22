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
 * 数据源缓存 VO ── 对齐 {@link DataBridgeCacheVO} / {@code DeviceCacheVO} 模式。
 * <p>
 * 字段保持与 {@code DataSource} 实体的 getter 方法名完全一致,让 SinkDispatcher /
 * BridgeRetryPolicyResolver 等热路径的 {@code ds.getXxx()} 调用与原 entity 兼容,无需改业务代码。
 * <p>
 * 相比 entity 去掉:
 * <ul>
 *   <li>@TableField / @TableName / @EncryptTypeHandler 等 ORM 包袱</li>
 *   <li>{@code lastHealthCheckTime}(健康检查频繁更新会污染缓存; SinkDispatcher 不读)</li>
 *   <li>{@code remark}(UI 列表展示用,非热路径)</li>
 *   <li>审计字段 {@code createdOrgId / createdBy / createdTime / updatedBy / updatedTime / deleted}</li>
 * </ul>
 * <p>
 * 注:{@code connectionJson / credentialJson} 保留为<b>明文</b>(Service 层从 entity 取出时
 * 已被 {@code EncryptTypeHandler} 自动解密),Connector 实例化时直接用,不再二次解密。
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
@Schema(title = "DataSourceCacheVO", description = "数据源缓存 VO")
public class DataSourceCacheVO extends Entity<Long> implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "id")
    private Long id;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "数据源名称")
    private String dataSourceName;

    @Schema(description = "业务唯一编码")
    private String dataSourceCode;

    @Schema(description = "方向：10-出站sink / 20-入站source / 30-双向")
    private String direction;

    @Schema(description = "协议类型：KAFKA/REDIS/ROCKETMQ/RABBITMQ/MYSQL/HTTP/WEBHOOK/MQTT")
    private String sourceType;

    @Schema(description = "连接参数 JSON(已解密;Connector 直接用)")
    private String connectionJson;

    @Schema(description = "凭证 JSON(已解密;Connector 直接用)")
    private String credentialJson;

    @Schema(description = "序列化策略:JSON/AVRO/STRING/BINARY")
    private String serialization;

    @Schema(description = "默认可靠性级别(规则可覆盖)")
    private Integer defaultQos;

    @Schema(description = "默认 QPS 限流(0=不限)")
    private Integer defaultRateLimitQps;

    @Schema(description = "默认最大重试次数")
    private Integer defaultRetryMaxTimes;

    @Schema(description = "默认初始退避时长 ms")
    private Integer defaultRetryBackoffMs;

    @Schema(description = "默认单次发送超时 ms")
    private Integer defaultTimeoutMs;

    @Schema(description = "默认死信投递的数据源 FK")
    private Long defaultDeadLetterDataSourceId;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "健康状态:HEALTHY/DEGRADED/DOWN/UNKNOWN")
    private String healthStatus;

    @Schema(description = "扩展参数(协议特异调参 JSON)")
    private String extendParams;
}
