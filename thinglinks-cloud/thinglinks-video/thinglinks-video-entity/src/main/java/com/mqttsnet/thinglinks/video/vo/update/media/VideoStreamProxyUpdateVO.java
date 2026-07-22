package com.mqttsnet.thinglinks.video.vo.update.media;

import com.mqttsnet.basic.base.entity.SuperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 表单修改方法VO
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
@EqualsAndHashCode
@Builder
@Schema(description = "视频拉流代理信息表")
public class VideoStreamProxyUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "唯一标识符")
    @NotNull(message = "请填写唯一标识符", groups = SuperEntity.Update.class)
    private Long id;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    @NotEmpty(message = "请填写应用ID")
    @Size(max = 64, message = "应用ID长度不能超过{max}")
    private String appId;
    /**
     * 代理类型
     */
    @Schema(description = "代理类型")
    @Size(max = 50, message = "代理类型长度不能超过{max}")
    private String proxyType;
    /**
     * 代理名称
     */
    @Schema(description = "代理名称")
    @Size(max = 255, message = "代理名称长度不能超过{max}")
    private String proxyName;
    /**
     * 流唯一标识
     */
    @Schema(description = "流唯一标识")
    @Size(max = 255, message = "流唯一标识长度不能超过{max}")
    private String streamIdentification;
    /**
     * 拉流地址
     */
    @Schema(description = "拉流地址")
    @Size(max = 255, message = "拉流地址长度不能超过{max}")
    private String url;
    /**
     * 源地址
     */
    @Schema(description = "源地址")
    @Size(max = 255, message = "源地址长度不能超过{max}")
    private String srcUrl;
    /**
     * 目标地址
     */
    @Schema(description = "目标地址")
    @Size(max = 255, message = "目标地址长度不能超过{max}")
    private String dstUrl;
    /**
     * 超时时间（毫秒）
     */
    @Schema(description = "超时时间（毫秒）")
    private Integer timeoutMs;
    /**
     * FFmpeg模板KEY
     */
    @Schema(description = "FFmpeg模板KEY")
    @Size(max = 255, message = "FFmpeg模板KEY长度不能超过{max}")
    private String ffmpegCmdKey;
    /**
     * RTSP拉流时的拉流方式（0：TCP，1：UDP，2：组播）
     */
    @Schema(description = "RTSP拉流时的拉流方式（0：TCP，1：UDP，2：组播）")
    @Size(max = 50, message = "RTSP拉流时的拉流方式（0：TCP，1：UDP，2：组播）长度不能超过{max}")
    private String rtpType;
    /**
     * 国标唯一标识
     */
    @Schema(description = "国标唯一标识")
    @Size(max = 255, message = "国标唯一标识长度不能超过{max}")
    private String gbIdentification;
    /**
     * 媒体唯一标识
     */
    @Schema(description = "媒体唯一标识")
    @NotEmpty(message = "请填写媒体唯一标识")
    @Size(max = 255, message = "媒体唯一标识长度不能超过{max}")
    private String mediaIdentification;
    /**
     * 是否启用音频
     */
    @Schema(description = "是否启用音频")
    private Boolean enableAudio;
    /**
     * 是否启用MP4
     */
    @Schema(description = "是否启用MP4")
    private Boolean enableMp4;
    /**
     * 状态
     */
    @Schema(description = "状态")
    private Boolean status;
    /**
     * 无人观看时是否删除
     */
    @Schema(description = "无人观看时是否删除")
    private Boolean enableRemoveNoneReader;
    /**
     * 拉流代理时ZLM返回的KEY，用于停止拉流代理
     */
    @Schema(description = "拉流代理时ZLM返回的KEY，用于停止拉流代理")
    @Size(max = 255, message = "拉流代理时ZLM返回的KEY，用于停止拉流代理长度不能超过{max}")
    private String streamKey;
    /**
     * 无人观看时是否自动停用
     */
    @Schema(description = "无人观看时是否自动停用")
    private Boolean enableDisableNoneReader;
    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    @Size(max = 2147483647, message = "扩展参数长度不能超过{max}")
    private String extendParams;
    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;
    /**
     * 最大重试次数
     */
    @Schema(description = "最大重试次数")
    private Integer maxRetryCount;


}
