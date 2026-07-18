package com.mqttsnet.thinglinks.video.vo.result.platform;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>
 * 级联平台表 返回结果对象
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-01 00:00:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "级联平台表返回结果对象")
public class VideoPlatformResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "启用状态")
    private Boolean enable;

    @Schema(description = "上级平台国标编号")
    private String serverGbId;

    @Schema(description = "上级平台国标域")
    private String serverGbDomain;

    @Schema(description = "上级平台地址")
    private String serverIp;

    @Schema(description = "上级平台端口")
    private Integer serverPort;

    @Schema(description = "本平台国标编号")
    private String deviceGbId;

    @Schema(description = "本平台地址")
    private String deviceIp;

    @Schema(description = "本平台端口")
    private Integer devicePort;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "注册有效期(秒)")
    private Integer expires;

    @Schema(description = "心跳周期(秒)")
    private Integer keepTimeout;

    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_DEVICE_TRANSPORT)
    @Schema(description = "传输协议")
    private String transport;

    @Schema(description = "字符集")
    private String characterSet;

    @Schema(description = "是否共享PTZ")
    private Boolean ptz;

    @Schema(description = "是否共享RTCP")
    private Boolean rtcp;

    @Schema(description = "状态")
    private Boolean status;

    @Schema(description = "目录订阅")
    private Boolean catalogSubscribe;

    @Schema(description = "告警订阅")
    private Boolean alarmSubscribe;

    @Schema(description = "位置订阅")
    private Boolean mobilePositionSubscribe;

    @Schema(description = "目录分组")
    private Integer catalogGroup;

    @Schema(description = "作为消息通道")
    private Boolean asMessageChannel;

    @Schema(description = "推流IP")
    private String sendStreamIp;

    @Schema(description = "自动推送通道")
    private Boolean autoPushChannel;

    @Schema(description = "关联平台目录")
    private Integer catalogWithPlatform;

    @Schema(description = "关联分组目录")
    private Integer catalogWithGroup;

    @Schema(description = "关联区域目录")
    private Integer catalogWithRegion;

    @Schema(description = "行政区划")
    private String civilCode;

    @Schema(description = "厂商")
    private String manufacturer;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "地址")
    private String address;

    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_GB28181_SIP_REGISTER_WAY)
    @Schema(description = "注册方式")
    private Integer registerWay;

    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_DEVICE_SECRECY)
    @Schema(description = "保密属性")
    private Integer secrecy;

    @Schema(description = "服务器ID")
    private String serverId;

    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_PLATFORM_CASCADE_TYPE)
    @Schema(description = "级联类型")
    private Integer cascadeType;

    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_PLATFORM_GB_VERSION)
    @Schema(description = "国标版本")
    private String gbVersion;

    @Schema(description = "在线状态")
    private Boolean onlineStatus;

    @Schema(description = "注册过期时间")
    private Integer registerExpires;

    @Schema(description = "心跳间隔")
    private Integer keepaliveInterval;

    @Schema(description = "心跳超时次数")
    private Integer keepaliveTimeoutCount;

    @Schema(description = "最近注册时间")
    private String lastRegisterTime;

    @Schema(description = "最近心跳时间")
    private String lastKeepaliveTime;

    @Schema(description = "离线推送")
    private Boolean startOfflinePush;

    @Schema(description = "SIP地址")
    private String sipIp;

    @Schema(description = "SIP端口")
    private Integer sipPort;

    @Schema(description = "Hook地址前缀")
    private String hookUrlPrefix;

    @Schema(description = "服务实例ID")
    private String serviceInstanceId;

    @Schema(description = "级联SDP地址")
    private String cascadeSdpIp;

    @Schema(description = "删除标记")
    private Integer deleted;
}
