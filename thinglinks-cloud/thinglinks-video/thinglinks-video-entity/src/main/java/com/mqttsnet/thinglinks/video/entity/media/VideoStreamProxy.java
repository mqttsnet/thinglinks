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
 * 视频拉流代理信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-05 22:32:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName("video_stream_proxy")
public class VideoStreamProxy extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @TableField(value = "app_id", condition = LIKE)
    private String appId;
    /**
     * 代理类型
     */
    @TableField(value = "proxy_type", condition = LIKE)
    private String proxyType;
    /**
     * 代理名称
     */
    @TableField(value = "proxy_name", condition = LIKE)
    private String proxyName;
    /**
     * 流唯一标识
     */
    @TableField(value = "stream_identification", condition = LIKE)
    private String streamIdentification;
    /**
     * 拉流地址
     */
    @TableField(value = "url", condition = LIKE)
    private String url;
    /**
     * 源地址
     */
    @TableField(value = "src_url", condition = LIKE)
    private String srcUrl;
    /**
     * 目标地址
     */
    @TableField(value = "dst_url", condition = LIKE)
    private String dstUrl;
    /**
     * 超时时间（毫秒）
     */
    @TableField(value = "timeout_ms", condition = EQUAL)
    private Integer timeoutMs;
    /**
     * FFmpeg模板KEY
     */
    @TableField(value = "ffmpeg_cmd_key", condition = LIKE)
    private String ffmpegCmdKey;
    /**
     * RTSP拉流时的拉流方式（0：TCP，1：UDP，2：组播）
     */
    @TableField(value = "rtp_type", condition = LIKE)
    private String rtpType;
    /**
     * 国标唯一标识
     */
    @TableField(value = "gb_identification", condition = LIKE)
    private String gbIdentification;
    /**
     * 媒体唯一标识
     */
    @TableField(value = "media_identification", condition = LIKE)
    private String mediaIdentification;
    /**
     * 是否启用音频
     */
    @TableField(value = "enable_audio", condition = EQUAL)
    private Boolean enableAudio;
    /**
     * 是否启用MP4
     */
    @TableField(value = "enable_mp4", condition = EQUAL)
    private Boolean enableMp4;
    /**
     * 状态
     */
    @TableField(value = "status", condition = EQUAL)
    private Boolean status;
    /**
     * 无人观看时是否删除
     */
    @TableField(value = "enable_remove_none_reader", condition = EQUAL)
    private Boolean enableRemoveNoneReader;
    /**
     * 拉流代理时ZLM返回的KEY，用于停止拉流代理
     */
    @TableField(value = "stream_key", condition = LIKE)
    private String streamKey;
    /**
     * 无人观看时是否自动停用
     */
    @TableField(value = "enable_disable_none_reader", condition = EQUAL)
    private Boolean enableDisableNoneReader;
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
     * 拉流重试次数
     */
    @TableField(value = "pull_retry_count", condition = EQUAL)
    private Integer pullRetryCount;

    /**
     * 最大重试次数
     */
    @TableField(value = "max_retry_count", condition = EQUAL)
    private Integer maxRetryCount;

    /**
     * 最近拉流时间
     */
    @TableField(value = "last_pull_time", condition = EQUAL)
    private LocalDateTime lastPullTime;

    /**
     * 最近错误信息
     */
    @TableField(value = "last_error", condition = LIKE)
    private String lastError;

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
