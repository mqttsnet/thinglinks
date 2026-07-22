package com.mqttsnet.thinglinks.video.vo.result.media;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import com.mqttsnet.thinglinks.video.vo.result.media.zlm.ZlmMediaServerStreamInfoResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 表单查询方法返回值VO
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
@Schema(description = "视频推流信息表")
public class VideoStreamPushResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String appId;
    /**
     * 流唯一标识
     */
    @Schema(description = "流唯一标识")
    private String streamIdentification;
    /**
     * 观看总人数
     */
    @Schema(description = "观看总人数")
    private Integer totalReaderCount;
    /**
     * 产生源类型
     */
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_MEDIA_ORIGIN_TYPE)
    @Schema(description = "产生源类型", title = "unknown = 0,rtmp_push=1,rtsp_push=2,rtp_push=3,pull=4,ffmpeg_pull=5,mp4_vod=6,device_chn=7")
    private Integer originType;
    /**
     * 产生源的url
     */
    @Schema(description = "产生源的url")
    private String originUrl;
    /**
     * 音视频轨道
     */
    @Schema(description = "音视频轨道")
    private String vhost;
    /**
     * 数据产生速度，单位byte/s
     */
    @Schema(description = "数据产生速度，单位byte/s")
    private Double bytesSpeed;
    /**
     * 存活时间，单位秒
     */
    @Schema(description = "存活时间，单位秒")
    private Long aliveSecond;
    /**
     * 媒体唯一标识
     */
    @Schema(description = "媒体唯一标识")
    private String mediaIdentification;
    /**
     * 使用的服务ID
     */
    @Schema(description = "使用的服务ID")
    private String serverId;
    /**
     * 推流时间
     */
    @Schema(description = "推流时间")
    private LocalDateTime pushTime;
    /**
     * 状态
     */
    @Schema(description = "状态")
    private Boolean status;
    /**
     * 是否正在推流
     */
    @Schema(description = "是否正在推流")
    private Boolean pushIng;
    /**
     * 是否自己平台的推流
     */
    @Schema(description = "是否自己平台的推流")
    private Boolean self;
    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private String extendParams;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * ZLM流媒体信息集合
     */
    @Schema(description = "ZLM流媒体信息集合")
    private List<ZlmMediaServerStreamInfoResultVO> zlmMediaServerStreamInfoList;

    /**
     * 推流入口地址（RTMP）。运维/用户拿这个 URL 配置 OBS / FFmpeg 推流，是推流功能能跑通的关键。
     * 后端按 rtmp://<streamHost>:<rtmpPort>/<appId>/<streamIdentification> 拼装。
     */
    @Schema(description = "推流入口地址（RTMP）")
    private String pushUrl;

    /**
     * 推流入口地址（RTSP），适用于支持 RTSP ANNOUNCE 的设备。
     */
    @Schema(description = "推流入口地址（RTSP）")
    private String pushUrlRtsp;

}
