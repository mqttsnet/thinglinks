package com.mqttsnet.thinglinks.video.entity.device;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Description:
 * 视频事件通知订阅配置表实体。
 * <p>
 * 一行 = 一条通知路径: 事件类型 + 渠道 + 模板 + 接收人。
 * 运行时通过 channel_config (configList) 覆盖 DefInterfaceProperty 凭证。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@TableName(value = "video_notify_subscription", autoResultMap = true)
public class VideoNotifySubscription extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订阅名称(如: 运维钉钉群-严重告警)
     */
    @TableField(value = "subscription_name", condition = LIKE)
    private String subscriptionName;

    /**
     * 渠道类型(字典 NOTIFY_CHANNEL_TYPE): NOTICE/DINGTALK/FEISHU/ENTERPRISE_WECHAT/SMS
     */
    @TableField(value = "channel_type", condition = EQUAL)
    private String channelType;

    /**
     * 渠道凭证(JSON): 运行时通过 configList 覆盖 DefInterfaceProperty
     * 钉钉: {"token":"xxx","secret":"xxx"}
     * 飞书: {"appId":"xxx","appSecret":"xxx","token":"xxx"}
     */
    @TableField(value = "channel_config")
    private String channelConfig;

    /**
     * 消息模板编码(关联 ExtendMsgTemplate.code)
     */
    @TableField(value = "template_code", condition = EQUAL)
    private String templateCode;

    /**
     * 订阅事件类型(逗号分隔): ALARM,DEVICE_ONLINE,DEVICE_OFFLINE,STREAM_CLOSE
     */
    @TableField(value = "event_types", condition = LIKE)
    private String eventTypes;

    /**
     * 告警级别过滤(逗号分隔,空=全部): 1,2,3,4
     */
    @TableField(value = "priority_filter")
    private String priorityFilter;

    /**
     * 站内信接收范围: SELF=创建人/ORG=创建人组织/CUSTOM=本部门自定义
     */
    @TableField(value = "recipient_scope", condition = EQUAL)
    private String recipientScope;

    /**
     * 接收人用户ID(逗号分隔, CUSTOM时使用, 限本部门)
     */
    @TableField(value = "recipient_ids")
    private String recipientIds;

    /**
     * @所有人(0=否/1=是, 钉钉/飞书群用)
     */
    @TableField(value = "at_all", condition = EQUAL)
    private Integer atAll;

    /**
     * 跳转链接模板: ${sys.domain}/video/alarm?id=${bizId}
     */
    @TableField(value = "jump_url_template")
    private String jumpUrlTemplate;

    /**
     * 消息内容模板(覆盖 ExtendMsgTemplate.content, 支持${变量})
     */
    @TableField(value = "msg_template")
    private String msgTemplate;

    /**
     * 状态(0=禁用/1=启用)
     */
    @TableField(value = "status", condition = EQUAL)
    private Integer status;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 创建人组织
     */
    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
