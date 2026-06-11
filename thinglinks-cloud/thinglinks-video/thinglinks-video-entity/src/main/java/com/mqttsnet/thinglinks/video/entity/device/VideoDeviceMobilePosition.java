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
 * 设备移动位置信息表实体。
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
@TableName(value = "video_device_mobile_position", autoResultMap = true)
public class VideoDeviceMobilePosition extends Entity<Long> {
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
     * 海拔高度（米）
     */
    @TableField(value = "altitude", condition = EQUAL)
    private Double altitude;

    /**
     * 速度（km/h）
     */
    @TableField(value = "speed", condition = EQUAL)
    private Double speed;

    /**
     * 方向（度）
     */
    @TableField(value = "direction", condition = EQUAL)
    private Double direction;

    /**
     * 上报时间
     */
    @TableField(value = "report_time")
    private LocalDateTime reportTime;

    /**
     * 地理坐标系
     */
    @TableField(value = "geo_coord_sys", condition = LIKE)
    private String geoCoordSys;

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
