package com.mqttsnet.thinglinks.video.vo.result.device;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
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

/**
 * <p>
 * 表单查询方法返回值VO
 * 视频设备信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2026-03-31 00:00:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "视频设备信息表")
public class VideoDeviceResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "唯一标识符")
    private Long id;

    /**
     * 设备唯一标识
     */
    @Schema(description = "设备唯一标识")
    private String deviceIdentification;
    /**
     * 接入协议
     */
    @Schema(description = "接入协议")
    private String accessProtocol;
    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;
    /**
     * 自定义别名
     */
    @Schema(description = "自定义别名")
    private String customName;
    /**
     * 厂商
     */
    @Schema(description = "厂商")
    private String manufacturer;
    /**
     * 型号
     */
    @Schema(description = "型号")
    private String model;
    /**
     * 固件版本
     */
    @Schema(description = "固件版本")
    private String firmware;
    /**
     * 设备地址(IP/域名)
     */
    @Schema(description = "设备地址(IP/域名)")
    private String host;
    /**
     * 端口
     */
    @Schema(description = "端口")
    private Integer port;
    /**
     * 公网地址(IP/域名)
     */
    @Schema(description = "公网地址(IP/域名)")
    private String wanHost;
    /**
     * 局域网地址(IP/域名)
     */
    @Schema(description = "局域网地址(IP/域名)")
    private String lanHost;
    /**
     * 完整访问端点(host:port)
     */
    @Schema(description = "完整访问端点(host:port)")
    private String accessEndpoint;
    /**
     * 收流地址(IP/域名)
     */
    @Schema(description = "收流地址(IP/域名)")
    private String sdpHost;
    /**
     * 本地SIP交互地址(IP/域名)
     */
    @Schema(description = "本地SIP交互地址(IP/域名)")
    private String localHost;
    /**
     * 传输协议
     */
    @Schema(description = "传输协议")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_DEVICE_TRANSPORT)
    private String transport;
    /**
     * 数据流传输模式
     */
    @Schema(description = "数据流传输模式")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_DEVICE_STREAM_MODE)
    private String streamMode;
    /**
     * 在线状态
     */
    @Schema(description = "在线状态")
    private Boolean onlineStatus;
    /**
     * 注册时间
     */
    @Schema(description = "注册时间")
    private String registerTime;
    /**
     * 最近心跳时间
     */
    @Schema(description = "最近心跳时间")
    private String lastKeepaliveTime;
    /**
     * 注册有效期
     */
    @Schema(description = "注册有效期")
    private Integer expires;
    /**
     * 心跳间隔
     */
    @Schema(description = "心跳间隔")
    private Integer keepaliveInterval;
    /**
     * 心跳超时次数
     */
    @Schema(description = "心跳超时次数")
    private Integer keepaliveTimeoutCount;
    /**
     * 认证方式
     */
    @Schema(description = "认证方式")
    private String authType;
    /**
     * 认证凭据
     */
    @Schema(description = "认证凭据")
    private String authSecret;
    /**
     * 关联流媒体标识
     */
    @Schema(description = "关联流媒体标识")
    private String mediaIdentification;
    /**
     * 通道数
     */
    @Schema(description = "通道数")
    private Integer channelCount;
    /**
     * 能力集
     */
    @Schema(description = "能力集")
    private String ability;
    /**
     * 协议专属配置
     */
    @Schema(description = "协议专属配置")
    private VideoDeviceProtocolConfig protocolConfig;
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
     * 逻辑删除
     */
    @Schema(description = "逻辑删除")
    private Integer deleted;


}
