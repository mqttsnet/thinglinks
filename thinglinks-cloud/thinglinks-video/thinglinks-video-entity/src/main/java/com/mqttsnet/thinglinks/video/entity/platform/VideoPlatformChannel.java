package com.mqttsnet.thinglinks.video.entity.platform;

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
 * 级联平台通道关联实体。
 * 记录哪些设备通道共享给了哪个上级平台。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@TableName(value = "video_platform_channel", autoResultMap = true)
public class VideoPlatformChannel extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(value = "platform_id", condition = EQUAL)
    private Long platformId;

    @TableField(value = "device_channel_id", condition = EQUAL)
    private Long deviceChannelId;

    @TableField(value = "catalog_id", condition = EQUAL)
    private Long catalogId;

    @TableField(value = "device_identification", condition = LIKE)
    private String deviceIdentification;

    @TableField(value = "channel_identification", condition = LIKE)
    private String channelIdentification;

    @TableField(value = "custom_name", condition = LIKE)
    private String customName;

    @TableField(value = "custom_gb_id", condition = LIKE)
    private String customGbId;

    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识:0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
