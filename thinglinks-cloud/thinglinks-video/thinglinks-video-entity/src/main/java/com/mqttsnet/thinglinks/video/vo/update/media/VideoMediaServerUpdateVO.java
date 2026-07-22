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
@EqualsAndHashCode
@Builder
@Schema(description = "流媒体服务器信息表")
public class VideoMediaServerUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @NotNull(message = "请填写id", groups = SuperEntity.Update.class)
    private Long id;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    @NotEmpty(message = "请填写应用ID")
    @Size(max = 64, message = "应用ID长度不能超过{max}")
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
    @Size(max = 50, message = "服务器地址长度不能超过{max}")
    private String host;
    /**
     * Hook回调地址(IP/域名)
     */
    @Schema(description = "Hook回调地址(IP/域名)")
    @Size(max = 50, message = "Hook回调地址长度不能超过{max}")
    private String hookHost;
    /**
     * SDP地址(IP/域名)
     */
    @Schema(description = "SDP地址(IP/域名)")
    @Size(max = 50, message = "SDP地址长度不能超过{max}")
    private String sdpHost;
    /**
     * 流播放地址(IP/域名)
     */
    @Schema(description = "流播放地址(IP/域名)")
    @Size(max = 50, message = "流播放地址长度不能超过{max}")
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
    @Size(max = 50, message = "ZLM鉴权参数长度不能超过{max}")
    private String secret;
    /**
     * 类型（zlm/abl）
     */
    @Schema(description = "类型（zlm/abl）")
    @Size(max = 50, message = "类型（zlm/abl）长度不能超过{max}")
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
    @Size(max = 50, message = "多端口RTP收流端口范围长度不能超过{max}")
    private String rtpPortRange;
    /**
     * RTP发流端口范围
     */
    @Schema(description = "RTP发流端口范围")
    @Size(max = 50, message = "RTP发流端口范围长度不能超过{max}")
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
     * keepalive hook触发间隔，单位秒
     */
    @Schema(description = "keepalive hook触发间隔，单位秒")
    private Integer hookAliveInterval;
    /**
     * 录像存储路径
     */
    @Schema(description = "录像存储路径")
    @Size(max = 255, message = "录像存储路径长度不能超过{max}")
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
    @Size(max = 255, message = "转码的前缀长度不能超过{max}")
    private String transcodeSuffix;
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
     * 在线状态
     */
    @Schema(description = "在线状态")
    private Boolean onlineStatus;
    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    @Size(max = 2147483647, message = "扩展参数长度不能超过{max}")
    private String extendParams;
    /**
     * 名称
     */
    @Schema(description = "多媒体名称")
    @Size(max = 255, message = "名称长度不能超过{max}")
    private String name;
    /**
     * 服务器版本号
     */
    @Schema(description = "服务器版本号")
    @Size(max = 50, message = "服务器版本号长度不能超过{max}")
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


}
