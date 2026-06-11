package com.mqttsnet.thinglinks.video.entity.device;

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
 * 设备告警信息表实体。
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
@TableName(value = "video_device_alarm", autoResultMap = true)
public class VideoDeviceAlarm extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备国标编号
     */
    @TableField(value = "device_identification", condition = LIKE)
    private String deviceIdentification;

    /**
     * 通道国标编号
     */
    @TableField(value = "channel_identification", condition = LIKE)
    private String channelIdentification;

    /**
     * 告警级别（1-一级警情/2-二级警情/3-三级警情/4-四级警情）
     */
    @TableField(value = "alarm_priority", condition = EQUAL)
    private Integer alarmPriority;

    /**
     * 告警方式（1-电话/2-设备/3-短信/4-GPS/5-视频/6-设备故障/7-其他）
     */
    @TableField(value = "alarm_method", condition = EQUAL)
    private Integer alarmMethod;

    /**
     * 告警时间
     */
    @TableField(value = "alarm_time")
    private LocalDateTime alarmTime;

    /**
     * 告警描述
     */
    @TableField(value = "alarm_description", condition = LIKE)
    private String alarmDescription;

    /**
     * 告警类型
     */
    @TableField(value = "alarm_type", condition = EQUAL)
    private Integer alarmType;

    /**
     * 告警类型参数（JSON）
     */
    @TableField(value = "alarm_type_param", condition = LIKE)
    private String alarmTypeParam;

    /**
     * 经度
     */
    @TableField(value = "longitude", condition = EQUAL)
    private Double longitude;

    /**
     * 纬度
     */
    @TableField(value = "latitude", condition = EQUAL)
    private Double latitude;

    /**
     * 处理状态（0-待处理/1-处理中/2-已处理/3-已忽略）
     */
    @TableField(value = "handle_status", condition = EQUAL)
    private Integer handleStatus;

    /**
     * 处理人ID (通过 @Echo 回显用户姓名，不单独存姓名)
     */
    @TableField(value = "handle_user_id")
    private Long handleUserId;

    /**
     * 处理时间
     */
    @TableField(value = "handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理结果描述
     */
    @TableField(value = "handle_result")
    private String handleResult;

    /**
     * 创建人组织
     */
    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识:0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
