package com.mqttsnet.thinglinks.video.entity.record;

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
 * 录像计划实体。
 * 对应数据库表 {@code video_record_plan}，用于管理云端录像的定时任务配置。
 * <p>
 * 字段说明：
 * <ul>
 *   <li>{@code planType} — 计划类型（0=设备录像，1=云端录像）</li>
 *   <li>{@code planStatus} — 计划状态（0=停用，1=启用）</li>
 *   <li>{@code scheduleRule} — 调度规则，JSON 格式，支持 weekly/cron/once</li>
 *   <li>{@code segmentDuration} — 录像分段时长（秒），默认 3600</li>
 *   <li>{@code retentionDays} — 录像保留天数，超期自动清理</li>
 * </ul>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @see com.mqttsnet.thinglinks.video.entity.record.VideoRecordFile
 * @since 2026-03-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@TableName(value = "video_record_plan", autoResultMap = true)
public class VideoRecordPlan extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(value = "plan_name", condition = LIKE)
    private String planName;

    @TableField(value = "plan_type", condition = EQUAL)
    private Integer planType;

    @TableField(value = "media_identification", condition = LIKE)
    private String mediaIdentification;

    @TableField(value = "record_format", condition = LIKE)
    private String recordFormat;

    @TableField(value = "segment_duration", condition = EQUAL)
    private Integer segmentDuration;

    @TableField(value = "retention_days", condition = EQUAL)
    private Integer retentionDays;

    @TableField(value = "storage_path", condition = LIKE)
    private String storagePath;

    @TableField(value = "plan_status", condition = EQUAL)
    private Integer planStatus;

    @TableField(value = "schedule_rule", condition = EQUAL)
    private String scheduleRule;

    @TableField(value = "extend_params", condition = EQUAL)
    private String extendParams;

    @TableField(value = "remark", condition = LIKE)
    private String remark;

    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识:0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
