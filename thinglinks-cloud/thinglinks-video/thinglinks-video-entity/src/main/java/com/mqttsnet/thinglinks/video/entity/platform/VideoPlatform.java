package com.mqttsnet.thinglinks.video.entity.platform;

import static com.baomidou.mybatisplus.annotation.SqlCondition.EQUAL;
import static com.mqttsnet.thinglinks.model.constant.Condition.LIKE;

import java.io.Serial;

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
 * 国标级联平台实体。
 *
 * <p>支持集群 Docker 部署下的 GB28181 级联能力：</p>
 * <ul>
 *   <li><b>服务实例绑定</b>：{@code serviceInstanceId} 标识哪个服务节点维持 SIP 级联会话，
 *       避免多节点重复注册</li>
 *   <li><b>SIP 地址隔离</b>：{@code sipIp}/{@code sipPort} 为每个集群节点配置独立的
 *       SIP 监听地址，支持多节点同时维持不同平台的级联</li>
 *   <li><b>网关路由</b>：{@code hookUrlPrefix} 指定 Hook 回调通过网关路由，
 *       上级平台 INVITE 请求经网关负载均衡到具体节点</li>
 *   <li><b>注册续约</b>：{@code registerExpires}/{@code keepaliveInterval}/{@code keepaliveTimeoutCount}
 *       控制级联注册定时续约和心跳检测</li>
 * </ul>
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
@TableName(value = "video_platform", autoResultMap = true)
public class VideoPlatform extends Entity<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(value = "name", condition = LIKE)
    private String name;

    @TableField(value = "enable", condition = EQUAL)
    private Boolean enable;

    @TableField(value = "server_gb_id", condition = LIKE)
    private String serverGbId;

    @TableField(value = "server_gb_domain", condition = LIKE)
    private String serverGbDomain;

    /**
     * SIP服务IP/域名
     */
    @TableField(value = "server_ip", condition = LIKE)
    private String serverIp;

    @TableField(value = "server_port", condition = EQUAL)
    private Integer serverPort;

    @TableField(value = "device_gb_id", condition = LIKE)
    private String deviceGbId;

    /**
     * 设备IP/域名
     */
    @TableField(value = "device_ip", condition = LIKE)
    private String deviceIp;

    @TableField(value = "device_port", condition = EQUAL)
    private Integer devicePort;

    @TableField(value = "username", condition = LIKE)
    private String username;

    @TableField(value = "password", condition = EQUAL)
    private String password;

    @TableField(value = "expires", condition = EQUAL)
    private Integer expires;

    @TableField(value = "keep_timeout", condition = EQUAL)
    private Integer keepTimeout;

    @TableField(value = "transport", condition = LIKE)
    private String transport;

    @TableField(value = "character_set", condition = LIKE)
    private String characterSet;

    @TableField(value = "ptz", condition = EQUAL)
    private Boolean ptz;

    @TableField(value = "rtcp", condition = EQUAL)
    private Boolean rtcp;

    @TableField(value = "status", condition = EQUAL)
    private Boolean status;

    @TableField(value = "catalog_subscribe", condition = EQUAL)
    private Boolean catalogSubscribe;

    @TableField(value = "alarm_subscribe", condition = EQUAL)
    private Boolean alarmSubscribe;

    @TableField(value = "mobile_position_subscribe", condition = EQUAL)
    private Boolean mobilePositionSubscribe;

    @TableField(value = "catalog_group", condition = EQUAL)
    private Integer catalogGroup;

    @TableField(value = "as_message_channel", condition = EQUAL)
    private Boolean asMessageChannel;

    /**
     * 推流IP/域名（点播回复200OK使用的地址）
     */
    @TableField(value = "send_stream_ip", condition = LIKE)
    private String sendStreamIp;

    @TableField(value = "auto_push_channel", condition = EQUAL)
    private Boolean autoPushChannel;

    @TableField(value = "catalog_with_platform", condition = EQUAL)
    private Integer catalogWithPlatform;

    @TableField(value = "catalog_with_group", condition = EQUAL)
    private Integer catalogWithGroup;

    @TableField(value = "catalog_with_region", condition = EQUAL)
    private Integer catalogWithRegion;

    @TableField(value = "civil_code", condition = LIKE)
    private String civilCode;

    @TableField(value = "manufacturer", condition = LIKE)
    private String manufacturer;

    @TableField(value = "model", condition = LIKE)
    private String model;

    @TableField(value = "address", condition = LIKE)
    private String address;

    @TableField(value = "register_way", condition = EQUAL)
    private Integer registerWay;

    @TableField(value = "secrecy", condition = EQUAL)
    private Integer secrecy;

    @TableField(value = "server_id", condition = LIKE)
    private String serverId;

    /**
     * 级联类型（0=作为下级向上注册/1=作为上级接收注册）
     */
    @TableField(value = "cascade_type", condition = EQUAL)
    private Integer cascadeType;
    /**
     * 级联协议版本（2016/2022）
     */
    @TableField(value = "gb_version", condition = LIKE)
    private String gbVersion;
    /**
     * 在线状态（true=在线/false=离线）
     */
    @TableField(value = "online_status", condition = EQUAL)
    private Boolean onlineStatus;
    /**
     * 注册有效期（秒）
     */
    @TableField(value = "register_expires", condition = EQUAL)
    private Integer registerExpires;
    /**
     * 心跳间隔（秒）
     */
    @TableField(value = "keepalive_interval", condition = EQUAL)
    private Integer keepaliveInterval;
    /**
     * 心跳超时次数
     */
    @TableField(value = "keepalive_timeout_count", condition = EQUAL)
    private Integer keepaliveTimeoutCount;
    /**
     * 最近注册时间
     */
    @TableField(value = "last_register_time", condition = LIKE)
    private String lastRegisterTime;
    /**
     * 最近心跳时间
     */
    @TableField(value = "last_keepalive_time", condition = LIKE)
    private String lastKeepaliveTime;
    /**
     * 推送离线通道（true=推送/false=不推送）
     */
    @TableField(value = "start_offline_push", condition = EQUAL)
    private Boolean startOfflinePush;
    /**
     * 级联 SIP 本地监听 IP/域名（集群环境下每个节点的 SIP 地址不同）
     */
    @TableField(value = "sip_ip", condition = LIKE)
    private String sipIp;
    /**
     * 级联 SIP 本地监听端口（集群环境下每个节点的 SIP 端口不同）
     */
    @TableField(value = "sip_port", condition = EQUAL)
    private Integer sipPort;
    /**
     * 级联 Hook 回调地址前缀（集群环境下通过网关路由，格式如 http://gateway:port/video）
     */
    @TableField(value = "hook_url_prefix", condition = LIKE)
    private String hookUrlPrefix;
    /**
     * 负责处理此级联的服务实例 ID（集群部署时标识哪个节点维持 SIP 会话）
     */
    @TableField(value = "service_instance_id", condition = LIKE)
    private String serviceInstanceId;
    /**
     * 级联流媒体 SDP IP/域名（级联推流时使用的地址，集群环境下可能不同于 sendStreamIp）
     */
    @TableField(value = "cascade_sdp_ip", condition = LIKE)
    private String cascadeSdpIp;

    @TableField(value = "created_org_id", condition = EQUAL)
    private Long createdOrgId;

    /**
     * 逻辑删除标识:0-未删除 1-已删除
     */
    @TableLogic
    @TableField(value = "deleted", condition = EQUAL)
    private Integer deleted;
}
