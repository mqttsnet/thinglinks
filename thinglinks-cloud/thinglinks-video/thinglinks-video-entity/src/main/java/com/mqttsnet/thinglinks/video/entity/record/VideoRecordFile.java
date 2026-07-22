package com.mqttsnet.thinglinks.video.entity.record;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;
import java.time.LocalDateTime;

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
 * 录像文件实体。
 * 对应数据库表 {@code video_record_file}，记录云端录像文件的元数据信息。
 * <p>
 * 字段说明：
 * <ul>
 *   <li>{@code fileId} — 文件ID，关联 base 服务 File 表，通过 FileFacade 获取文件 URL</li>
 *   <li>{@code thumbnailFileId} — 缩略图文件ID，同样关联 base 服务 File 表</li>
 *   <li>{@code fileStatus} — 文件状态（0=录制中，1=已完成，2=已过期，3=已删除）</li>
 *   <li>{@code duration} — 录像时长（秒）</li>
 * </ul>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @see VideoRecordPlan
 * @since 2026-03-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@TableName(value = "video_record_file", autoResultMap = true)
public class VideoRecordFile extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(value = "plan_id", condition = EQUAL)
    private Long planId;

    @TableField(value = "device_identification", condition = LIKE)
    private String deviceIdentification;

    @TableField(value = "channel_identification", condition = LIKE)
    private String channelIdentification;

    @TableField(value = "stream_identification", condition = LIKE)
    private String streamIdentification;

    @TableField(value = "app", condition = LIKE)
    private String app;

    @TableField(value = "media_identification", condition = LIKE)
    private String mediaIdentification;

    @TableField(value = "file_name", condition = LIKE)
    private String fileName;

    @TableField(value = "file_id", condition = EQUAL)
    private Long fileId;

    @TableField(value = "file_size", condition = EQUAL)
    private Long fileSize;

    @TableField(value = "file_format", condition = LIKE)
    private String fileFormat;

    @TableField(value = "duration", condition = EQUAL)
    private Integer duration;

    @TableField(value = "start_time", condition = EQUAL)
    private LocalDateTime startTime;

    @TableField(value = "end_time", condition = EQUAL)
    private LocalDateTime endTime;

    @TableField(value = "thumbnail_file_id", condition = EQUAL)
    private Long thumbnailFileId;

    @TableField(value = "file_status", condition = EQUAL)
    private Integer fileStatus;

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
