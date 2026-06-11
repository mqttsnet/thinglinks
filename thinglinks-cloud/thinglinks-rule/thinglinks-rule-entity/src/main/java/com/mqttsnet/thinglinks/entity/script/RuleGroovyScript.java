package com.mqttsnet.thinglinks.entity.script;

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
 * 实体类
 * 规则脚本表
 * </p>
 *
 * @author mqttsnet
 * @date 2025-03-24 09:54:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName("rule_groovy_script")
public class RuleGroovyScript extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 脚本名称
     */
    @TableField(value = "name", condition = LIKE)
    private String name;

    /**
     * 应用ID
     */
    @TableField(value = "app_id", condition = LIKE)
    private String appId;
    /**
     * 脚本类型
     */
    @TableField(value = "script_type", condition = LIKE)
    private String scriptType;
    /**
     * 渠道编码
     */
    @TableField(value = "channel_code", condition = LIKE)
    private String channelCode;
    /**
     * 产品标识
     */
    @TableField(value = "product_identification", condition = LIKE)
    private String productIdentification;
    /**
     * 主题模式
     */
    @TableField(value = "topic_pattern", condition = LIKE)
    private String topicPattern;
    /**
     * 是否启用 [0-禁用 1-启用]
     */
    @TableField(value = "enable", condition = EQUAL)
    private Boolean enable;
    /**
     * 脚本内容
     */
    @TableField(value = "script_content", condition = LIKE)
    private String scriptContent;
    /**
     * 扩展信息
     */
    @TableField(value = "extend_params", condition = LIKE)
    private String extendParams;
    /**
     * 版本号
     */
    @TableField(value = "object_version", condition = LIKE)
    private String objectVersion;
    /**
     * 备注
     */
    @TableField(value = "remark", condition = LIKE)
    private String remark;
    /**
     * 创建人组织
     */
    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;


}
