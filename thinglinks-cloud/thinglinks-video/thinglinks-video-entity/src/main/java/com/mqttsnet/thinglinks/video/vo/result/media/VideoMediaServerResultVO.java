package com.mqttsnet.thinglinks.video.vo.result.media;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 表单查询方法返回值VO
 * 流媒体服务器信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-03 17:56:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "流媒体服务器信息表")
public class VideoMediaServerResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "唯一标识符")
    private Long id;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String appId;

    /**
     * 媒体唯一标识
     */
    @Schema(description = "媒体唯一标识")
    private String mediaIdentification;

    /**
     * 服务器地址(IP/域名)
     */
    @Schema(description = "服务器地址(IP/域名)")
    private String host;
    /**
     * Hook回调地址(IP/域名)
     */
    @Schema(description = "Hook回调地址(IP/域名)")
    private String hookHost;
    /**
     * SDP地址(IP/域名)
     */
    @Schema(description = "SDP地址(IP/域名)")
    private String sdpHost;
    /**
     * 流播放地址(IP/域名)
     */
    @Schema(description = "流播放地址(IP/域名)")
    private String streamHost;
    /**
     * HTTP端口
     */
    @Schema(description = "HTTP端口")
    private Integer httpPort;
    /**
     * HTTPS端口
     */
    @Schema(description = "HTTPS端口")
    private Integer httpSslPort;
    /**
     * RTMP端口
     */
    @Schema(description = "RTMP端口")
    private Integer rtmpPort;
    /**
     * RTMP SSL端口
     */
    @Schema(description = "RTMP SSL端口")
    private Integer rtmpSslPort;
    /**
     * RTP代理端口（单端口模式）
     */
    @Schema(description = "RTP代理端口（单端口模式）")
    private Integer rtpProxyPort;
    /**
     * RTSP端口
     */
    @Schema(description = "RTSP端口")
    private Integer rtspPort;
    /**
     * RTSP SSL端口
     */
    @Schema(description = "RTSP SSL端口")
    private Integer rtspSslPort;
    /**
     * FLV端口
     */
    @Schema(description = "FLV端口")
    private Integer flvPort;
    /**
     * FLV SSL端口
     */
    @Schema(description = "FLV SSL端口")
    private Integer flvSslPort;
    /**
     * WebSocket FLV端口
     */
    @Schema(description = "WebSocket FLV端口")
    private Integer wsFlvPort;
    /**
     * WebSocket FLV SSL端口
     */
    @Schema(description = "WebSocket FLV SSL端口")
    private Integer wsFlvSslPort;
    /**
     * 是否开启自动配置ZLM
     */
    @Schema(description = "是否开启自动配置ZLM")
    private Boolean autoConfig;
    /**
     * ZLM鉴权参数
     */
    @Schema(description = "ZLM鉴权参数")
    private String secret;
    /**
     * 类型（zlm/abl）
     */
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_MEDIA_SERVER_TYPE)
    @Schema(description = "类型（zlm/abl）")
    private String type;
    /**
     * 是否启用多端口模式
     */
    @Schema(description = "是否启用多端口模式")
    private Boolean rtpEnable;
    /**
     * 多端口RTP收流端口范围
     */
    @Schema(description = "多端口RTP收流端口范围")
    private String rtpPortRange;
    /**
     * RTP发流端口范围
     */
    @Schema(description = "RTP发流端口范围")
    private String sendRtpPortRange;
    /**
     * 录制辅助服务端口
     */
    @Schema(description = "录制辅助服务端口")
    private Integer recordAssistPort;
    /**
     * 是否是默认ZLM服务器
     */
    @Schema(description = "是否是默认ZLM服务器")
    private Boolean defaultServer;

    /**
     * 上次心跳时间
     */
    @Schema(description = "上次心跳时间")
    private LocalDateTime lastAliveTime;
    /**
     * keepalive hook触发间隔，单位秒
     */
    @Schema(description = "keepalive hook触发间隔，单位秒")
    private Integer hookAliveInterval;
    /**
     * 录像存储路径
     */
    @Schema(description = "录像存储路径")
    private String recordPath;
    /**
     * 录像存储时长（天）
     */
    @Schema(description = "录像存储时长（天）")
    private Integer recordDay;
    /**
     * 转码的前缀
     */
    @Schema(description = "转码的前缀")
    private String transcodeSuffix;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 在线状态
     */
    @Schema(description = "在线状态")
    private Boolean onlineStatus;
    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private String extendParams;
    /**
     * 名称
     */
    @Schema(description = "多媒体名称")
    private String name;
    /**
     * 服务器版本号
     */
    @Schema(description = "服务器版本号")
    private String version;
    /**
     * 服务器能力集(JSON，标记支持哪些API)
     */
    @Schema(description = "服务器能力集(JSON，标记支持哪些API)")
    private String capabilities;
    /**
     * 最大承载流数量(用于负载均衡)
     */
    @Schema(description = "最大承载流数量(用于负载均衡)")
    private Integer maxStreams;
    /**
     * 当前流数量
     */
    @Schema(description = "当前流数量")
    private Integer currentStreams;
    /**
     * CPU使用率(心跳上报)
     */
    @Schema(description = "CPU使用率(心跳上报)")
    private BigDecimal cpuUsage;
    /**
     * 内存使用率(心跳上报)
     */
    @Schema(description = "内存使用率(心跳上报)")
    private BigDecimal memoryUsage;
    /**
     * 入网速率bytes/s(心跳上报)
     */
    @Schema(description = "入网速率bytes/s(心跳上报)")
    private Long networkInSpeed;
    /**
     * 出网速率bytes/s(心跳上报)
     */
    @Schema(description = "出网速率bytes/s(心跳上报)")
    private Long networkOutSpeed;


}
