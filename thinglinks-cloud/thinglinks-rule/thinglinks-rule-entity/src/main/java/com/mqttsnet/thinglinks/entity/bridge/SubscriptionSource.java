package com.mqttsnet.thinglinks.entity.bridge;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mqttsnet.basic.base.entity.Entity;
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
 * 实体类：数据桥接-订阅源
 * 对应表 rule_subscription_source
 * </p>
 *
 * <p>本表无加密字段。凭证统一来自关联的 {@link DataSource}（direction=20 入站 / 30 双向）。</p>
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
@TableName("rule_subscription_source")
public class SubscriptionSource extends Entity<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID。
     */
    @TableField(value = "app_id", condition = LIKE)
    private String appId;

    /**
     * 订阅源名称（用户可读）。
     */
    @TableField(value = "source_name", condition = LIKE)
    private String sourceName;

    /**
     * 业务唯一编码（snowflake；HTTP 入站 endpoint URL 用此值）。
     */
    @TableField(value = "source_code", condition = EQUAL)
    private String sourceCode;

    /**
     * 复用数据源 FK→rule_data_source.id（direction 须为 20-入站 或 30-双向）。
     */
    @TableField(value = "data_source_id", condition = EQUAL)
    private Long dataSourceId;

    /**
     * 入站后处理方式：MQTT_FORWARD / RAW_INSERT / RULE_TRIGGER。
     */
    @TableField(value = "target_handler", condition = EQUAL)
    private String targetHandler;

    /**
     * 字段映射 JSON。
     */
    @TableField(value = "mapping_json")
    private String mappingJson;

    /**
     * target_handler=MQTT_FORWARD 时的目标产品标识。
     */
    @TableField(value = "target_product_identification", condition = LIKE)
    private String targetProductIdentification;

    /**
     * 目标 topic 模板（含 ${} 占位符）。
     */
    @TableField(value = "target_topic_template", condition = LIKE)
    private String targetTopicTemplate;

    /**
     * 是否启用：0-禁用 / 1-启用。
     */
    @TableField(value = "enable", condition = EQUAL)
    private Boolean enable;

    /**
     * 上次消费位点（Kafka offset / MQTT messageId / HTTP 时间戳；重启后接续消费）。
     */
    @TableField(value = "last_consume_offset")
    private String lastConsumeOffset;

    /**
     * 扩展参数。
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
