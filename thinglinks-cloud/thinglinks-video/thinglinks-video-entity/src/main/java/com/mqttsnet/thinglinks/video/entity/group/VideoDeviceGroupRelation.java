package com.mqttsnet.thinglinks.video.entity.group;

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
 * 设备分组关联实体。
 * 对应数据库表 {@code video_device_group_relation}，实现设备/通道与分组的多对多关联，
 * 支持同一设备出现在多个分组中，通过 {@code channelIdentification} 区分设备级和通道级关联。
 * <p>
 * 唯一约束：{@code (group_id, device_identification, channel_identification)}
 *
 * @author mqttsnet
 * @version 1.0.0
 * @see VideoDeviceGroup
 * @since 2026-03-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@TableName(value = "video_device_group_relation", autoResultMap = true)
public class VideoDeviceGroupRelation extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(value = "group_id", condition = EQUAL)
    private Long groupId;

    @TableField(value = "device_identification", condition = LIKE)
    private String deviceIdentification;

    @TableField(value = "channel_identification", condition = LIKE)
    private String channelIdentification;

    @TableField(value = "sort_order", condition = EQUAL)
    private Integer sortOrder;

    @TableField(value = "extend_params", condition = EQUAL)
    private String extendParams;

    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识:0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
