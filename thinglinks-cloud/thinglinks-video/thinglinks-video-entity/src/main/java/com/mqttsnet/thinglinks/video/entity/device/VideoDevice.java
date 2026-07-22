package com.mqttsnet.thinglinks.video.entity.device;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.basic.mybatis.typehandler.EncryptTypeHandler;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * <p>
 * 实体类
 * 统一设备表
 * </p>
 *
 * @author mqttsnet
 * @date 2025-05-15 17:00:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@TableName(value = "video_device", autoResultMap = true)
public class VideoDevice extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备标识
     */
    @TableField(value = "device_identification", condition = LIKE)
    private String deviceIdentification;
    /**
     * 设备接入协议（GB28181/ISUP/JT1078）
     */
    @TableField(value = "access_protocol", condition = LIKE)
    private String accessProtocol;
    /**
     * 设备名称
     */
    @TableField(value = "device_name", condition = LIKE)
    private String deviceName;
    /**
     * 自定义名称
     */
    @TableField(value = "custom_name", condition = LIKE)
    private String customName;
    /**
     * 厂商
     */
    @TableField(value = "manufacturer", condition = LIKE)
    private String manufacturer;
    /**
     * 型号
     */
    @TableField(value = "model", condition = LIKE)
    private String model;
    /**
     * 固件版本
     */
    @TableField(value = "firmware", condition = LIKE)
    private String firmware;
    /**
     * 设备地址(IP/域名)
     */
    @TableField(value = "host", condition = LIKE)
    private String host;
    /**
     * 端口
     */
    @TableField(value = "port", condition = EQUAL)
    private Integer port;
    /**
     * 公网地址(IP/域名)
     */
    @TableField(value = "wan_host", condition = LIKE)
    private String wanHost;
    /**
     * 局域网地址(IP/域名)
     */
    @TableField(value = "lan_host", condition = LIKE)
    private String lanHost;
    /**
     * 完整访问端点(host:port)
     */
    @TableField(value = "access_endpoint", condition = LIKE)
    private String accessEndpoint;
    /**
     * 收流地址(IP/域名)
     */
    @TableField(value = "sdp_host", condition = LIKE)
    private String sdpHost;
    /**
     * 本地SIP交互地址(IP/域名)
     */
    @TableField(value = "local_host", condition = LIKE)
    private String localHost;
    /**
     * 传输协议（UDP/TCP）
     */
    @TableField(value = "transport", condition = LIKE)
    private String transport;
    /**
     * 数据流传输模式
     */
    @TableField(value = "stream_mode", condition = LIKE)
    private String streamMode;
    /**
     * 是否在线
     */
    @TableField(value = "online_status", condition = EQUAL)
    private Boolean onlineStatus;
    /**
     * 注册时间
     */
    @TableField(value = "register_time", condition = LIKE)
    private String registerTime;
    /**
     * 最后心跳时间
     */
    @TableField(value = "last_keepalive_time", condition = LIKE)
    private String lastKeepaliveTime;
    /**
     * 注册有效期
     */
    @TableField(value = "expires", condition = EQUAL)
    private Integer expires;
    /**
     * 心跳间隔
     */
    @TableField(value = "keepalive_interval", condition = EQUAL)
    private Integer keepaliveInterval;
    /**
     * 心跳超时次数
     */
    @TableField(value = "keepalive_timeout_count", condition = EQUAL)
    private Integer keepaliveTimeoutCount;
    /**
     * 认证方式
     */
    @TableField(value = "auth_type", condition = LIKE)
    private String authType;
    /**
     * 认证密钥
     */
    @TableField(value = "auth_secret", condition = EQUAL, typeHandler = EncryptTypeHandler.class)
    private String authSecret;
    /**
     * 媒体唯一标识
     */
    @TableField(value = "media_identification", condition = LIKE)
    private String mediaIdentification;
    /**
     * 通道数量
     */
    @TableField(value = "channel_count", condition = EQUAL)
    private Integer channelCount;
    /**
     * 设备能力集描述
     */
    @TableField(value = "ability", condition = LIKE)
    private String ability;
    /**
     * 协议专属配置（类型安全视图，落库为 JSON 字符串）。
     * <p>记录 SIP 注册特征（User-Agent / Contact）、Catalog/DeviceInfo 同步时间戳等
     * 设备级协议元数据。字段级 merge 见 {@link VideoDeviceProtocolConfig#merge}，
     * 推荐通过 {@code VideoDeviceService.patchProtocolConfig} 按需更新。
     */
    @TableField(value = "protocol_config", typeHandler = Fastjson2TypeHandler.class)
    private VideoDeviceProtocolConfig protocolConfig;
    /**
     * 扩展参数（GB28181 设备级扩展字段的 JSON 字符串）。
     * <p>类型保持 {@link String} 以兼容 SQL {@code LIKE} 查询与系统内其他 entity 的约定；
     * 业务代码请用 {@link com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceExtendParams}
     * 的 {@code fromJson/toJsonString} 做视图转换，或走 {@code VideoDeviceService.patchExtendParams}
     * 按需合并。
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
