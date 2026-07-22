package com.mqttsnet.thinglinks.video.entity.media;

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

import java.io.Serial;
import java.time.LocalDateTime;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;


/**
 * <p>
 * 实体类
 * 视频推流信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-07 19:19:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName("video_stream_push")
public class VideoStreamPush extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @TableField(value = "app_id", condition = LIKE)
    private String appId;
    /**
     * 流唯一标识
     */
    @TableField(value = "stream_identification", condition = LIKE)
    private String streamIdentification;
    /**
     * 观看总人数
     */
    @TableField(value = "total_reader_count", condition = EQUAL)
    private Integer totalReaderCount;
    /**
     * 产生源类型
     */
    @TableField(value = "origin_type", condition = EQUAL)
    private Integer originType;
    /**
     * 产生源的url
     */
    @TableField(value = "origin_url", condition = LIKE)
    private String originUrl;
    /**
     * 音视频轨道
     */
    @TableField(value = "vhost", condition = LIKE)
    private String vhost;
    /**
     * 数据产生速度，单位byte/s
     */
    @TableField(value = "bytes_speed", condition = EQUAL)
    private Double bytesSpeed;
    /**
     * 存活时间，单位秒
     */
    @TableField(value = "alive_second", condition = EQUAL)
    private Long aliveSecond;
    /**
     * 媒体唯一标识
     */
    @TableField(value = "media_identification", condition = LIKE)
    private String mediaIdentification;
    /**
     * 使用的服务ID
     */
    @TableField(value = "server_id", condition = LIKE)
    private String serverId;
    /**
     * 推流时间
     */
    @TableField(value = "push_time", condition = EQUAL)
    private LocalDateTime pushTime;
    /**
     * 状态
     */
    @TableField(value = "status", condition = EQUAL)
    private Boolean status;
    /**
     * 是否正在推流
     */
    @TableField(value = "push_ing", condition = EQUAL)
    private Boolean pushIng;
    /**
     * 是否自己平台的推流
     */
    @TableField(value = "self", condition = EQUAL)
    private Boolean self;
    /**
     * 扩展参数
     */
    @TableField(value = "extend_params", condition = LIKE)
    private String extendParams;
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

    /**
     * 逻辑删除标识:0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;


}
