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

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

/**
 * <p>
 * 实体类：数据桥接-规则
 * 对应表 rule_data_bridge
 * </p>
 *
 * <h3>v3 三段 JSON 设计</h3>
 * <ul>
 *   <li>{@link #matchConfigJson}：匹配条件 ── 不加密（matcher 热路径要按内容查询，不含凭证）</li>
 *   <li>{@link #actionConfigJson}：动作配置 ── <b>EncryptTypeHandler 加密</b>（HTTP sink headers
 *       可能内联 Bearer token；MySQL columnMapping 可能含敏感 SQL）</li>
 * </ul>
 *
 * <h3>流控 / 重试两层 fallback</h3>
 * 规则字段 NOT NULL → 用规则值；NULL → fallback 到 {@link DataSource#getDefaultQos()} 等数据源默认值。
 * 实现见 {@code BridgeRetryPolicyResolver}。
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
@TableName(value = "rule_data_bridge", autoResultMap = true)
public class DataBridge extends Entity<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID。
     */
    @TableField(value = "app_id", condition = LIKE)
    private String appId;

    /**
     * 规则名称（列表页展示）。
     */
    @TableField(value = "rule_name", condition = LIKE)
    private String ruleName;

    /**
     * 规则业务唯一编码（snowflake）。
     */
    @TableField(value = "rule_code", condition = EQUAL)
    private String ruleCode;

    /**
     * 桥接方向：10-出站(平台→第三方) / 20-入站(第三方→平台)。
     */
    @TableField(value = "direction", condition = EQUAL)
    private String direction;

    /**
     * 关联数据源 FK→rule_data_source.id。
     */
    @TableField(value = "data_source_id", condition = EQUAL)
    private Long dataSourceId;

    /**
     * 匹配条件 JSON。
     * 出站含 productIdentifications/actionTypes/topicPatterns/deviceFilter/payloadFilter/timeWindow；
     * 入站含 subscriptionSourceIds/messageFilter。
     * <p>不加密：matcher 热路径要按内容查询。
     */
    @TableField(value = "match_config_json")
    private String matchConfigJson;

    /**
     * 动作配置 JSON。出站含 payloadTemplate/transformScript/sourceType 特异参数；
     * 入站含 targetHandler/fieldMapping。
     * <p><b>EncryptTypeHandler 加密落盘</b>：防御 HTTP sink 内联 Bearer token / MySQL columnMapping 敏感 SQL。
     */
    @TableField(value = "action_config_json", typeHandler = EncryptTypeHandler.class)
    private String actionConfigJson;

    /**
     * 规则级可靠性级别覆盖（NULL=用数据源默认）。
     */
    @TableField(value = "qos")
    private Integer qos;

    /**
     * 规则级 QPS 限流覆盖。
     */
    @TableField(value = "rate_limit_qps")
    private Integer rateLimitQps;

    /**
     * 规则级最大重试次数覆盖。
     */
    @TableField(value = "retry_max_times")
    private Integer retryMaxTimes;

    /**
     * 规则级初始退避时长覆盖（毫秒）。
     */
    @TableField(value = "retry_backoff_ms")
    private Integer retryBackoffMs;

    /**
     * 规则级单次发送超时覆盖（毫秒）。
     */
    @TableField(value = "timeout_ms")
    private Integer timeoutMs;

    /**
     * 规则级死信数据源覆盖。
     */
    @TableField(value = "dead_letter_data_source_id")
    private Long deadLetterDataSourceId;

    /**
     * 是否启用：0-禁用 / 1-启用（必须测试发送成功后手动启用）。
     */
    @TableField(value = "enable", condition = EQUAL)
    private Boolean enable;

    /**
     * 优先级（数字越小越先匹配）。
     */
    @TableField(value = "priority")
    private Integer priority;

    /**
     * 扩展参数（兜底，未来加加密/流量分级/A-B 灰度等 0 改表）。
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
