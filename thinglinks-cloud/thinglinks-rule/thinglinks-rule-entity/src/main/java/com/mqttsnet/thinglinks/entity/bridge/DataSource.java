package com.mqttsnet.thinglinks.entity.bridge;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.basic.mybatis.typehandler.EncryptTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

/**
 * <p>
 * 实体类：数据桥接-数据源（出/入站共用）
 * 对应表 rule_data_source
 * </p>
 *
 * <h3>加密字段</h3>
 * <ul>
 *   <li>{@link #connectionJson} 走 {@link EncryptTypeHandler} 整体加密落盘
 *       （JDBC URL 可能 ?password=、Redis URI 可能 redis://:pwd@host、HTTP URL 可能含 token）</li>
 *   <li>{@link #credentialJson} 同上（含 password / saslPassword / accessKey / secretKey 等核心机密）</li>
 * </ul>
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
@TableName(value = "rule_data_source", autoResultMap = true)
public class DataSource extends Entity<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID。
     */
    @TableField(value = "app_id", condition = LIKE)
    private String appId;

    /**
     * 数据源名称（用户起的友好标识，列表页显示）。
     */
    @TableField(value = "data_source_name", condition = LIKE)
    private String dataSourceName;

    /**
     * 业务唯一编码（snowflake，外部系统引用）。
     */
    @TableField(value = "data_source_code", condition = EQUAL)
    private String dataSourceCode;

    /**
     * 方向：10-出站sink / 20-入站source / 30-双向。
     */
    @TableField(value = "direction", condition = EQUAL)
    private String direction;

    /**
     * 协议类型：KAFKA/REDIS/ROCKETMQ/RABBITMQ/MYSQL/HTTP/WEBHOOK/MQTT。
     */
    @TableField(value = "source_type", condition = EQUAL)
    private String sourceType;

    /**
     * 连接参数 JSON（host/port/topic/database/mode 等）。
     * <b>整体 EncryptTypeHandler 加密落盘</b>，防御 JDBC URL / Redis URI 等可能内嵌的密码 / token。
     */
    @TableField(value = "connection_json", typeHandler = EncryptTypeHandler.class)
    private String connectionJson;

    /**
     * 凭证 JSON（password / saslPassword / accessKey / secretKey / bearerToken / HMAC secretKey 等）。
     * <b>整体 EncryptTypeHandler 加密落盘</b>，与 device.password 同规约。
     */
    @TableField(value = "credential_json", typeHandler = EncryptTypeHandler.class)
    private String credentialJson;

    /**
     * 序列化策略：JSON/AVRO/STRING/BINARY（与 Serializer.name() 1:1 对齐）。
     */
    @TableField(value = "serialization", condition = EQUAL)
    private String serialization;

    /**
     * 默认可靠性级别：0-fire-forget / 1-at-least-once / 2-exactly-once（规则可覆盖）。
     */
    @TableField(value = "default_qos")
    private Integer defaultQos;

    /**
     * 默认 QPS 限流（0=不限）。
     */
    @TableField(value = "default_rate_limit_qps")
    private Integer defaultRateLimitQps;

    /**
     * 默认最大重试次数。
     */
    @TableField(value = "default_retry_max_times")
    private Integer defaultRetryMaxTimes;

    /**
     * 默认初始退避时长 ms（指数倍增 1s/2s/4s/...）。
     */
    @TableField(value = "default_retry_backoff_ms")
    private Integer defaultRetryBackoffMs;

    /**
     * 默认单次发送超时 ms。
     */
    @TableField(value = "default_timeout_ms")
    private Integer defaultTimeoutMs;

    /**
     * 默认死信投递的数据源 FK。
     */
    @TableField(value = "default_dead_letter_data_source_id")
    private Long defaultDeadLetterDataSourceId;

    /**
     * 是否启用：0-禁用 / 1-启用（必须测试连接成功后手动启用）。
     */
    @TableField(value = "enable", condition = EQUAL)
    private Boolean enable;

    /**
     * 健康状态：HEALTHY/DEGRADED/DOWN/UNKNOWN。
     */
    @TableField(value = "health_status", condition = EQUAL)
    private String healthStatus;

    /**
     * 上次健康检查时间。
     */
    @TableField(value = "last_health_check_time")
    private LocalDateTime lastHealthCheckTime;

    /**
     * 扩展参数（协议特异调参 JSON）。
     */
    @TableField(value = "extend_params")
    private String extendParams;

    /**
     * 备注。
     */
    @TableField(value = "remark", condition = LIKE)
    private String remark;

    /**
     * 创建人组织。
     */
    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;
}
