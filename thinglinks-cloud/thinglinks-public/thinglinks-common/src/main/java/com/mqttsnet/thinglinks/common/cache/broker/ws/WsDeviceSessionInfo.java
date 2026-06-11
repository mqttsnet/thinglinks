package com.mqttsnet.thinglinks.common.cache.broker.ws;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WS 设备 session 全量信息(设备连接渠道信息)。
 *
 * <h3>双重身份</h3>
 * <ul>
 *   <li><b>本地鉴权门</b>:鉴权通过后存入 {@code jakarta.websocket.Session} 的 userProperties
 *       (key = {@link #SESSION_KEY}),随会话生命周期携带;约定<b>「上下文存在 == 已认证」</b>,
 *       {@code @OnMessage / @OnClose} 据此放行上行,免每条消息重复解析。</li>
 *   <li><b>多节点共享</b>:同一份信息以 JSON 存入 Redis(key 见 {@link WsDeviceSessionCacheKeyBuilder}),
 *       任一 broker / 业务节点都能据此判断设备是否在线、走什么渠道。</li>
 * </ul>
 *
 * <p><b>不用于下行路由</b> —— 下行命令走 RocketMQ 广播(各节点查本地 session 投递),不存节点地址、不点对点转发。
 *
 * @author mqttsnet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsDeviceSessionInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 存入 jakarta session userProperties 的 key(本地鉴权门用)。
     */
    public static final String SESSION_KEY = "wsDeviceSessionInfo";

    /**
     * 设备 clientId(含 {@code @租户} 后缀)。
     */
    private String clientId;

    /**
     * 租户 ID(字符串形式,与下游事件 / 缓存 key 命名空间对齐)。
     */
    private String tenantId;

    /**
     * 账号模式用户名。
     */
    private String username;

    /**
     * 鉴权结果带回的设备标识(下游直接用,免再解析)。
     */
    private String deviceIdentification;

    /**
     * 鉴权结果带回的产品标识。
     */
    private String productIdentification;

    /**
     * 接入协议(当前固定 {@code WEBSOCKET};预留多协议扩展)。
     */
    private String protocol;

    /**
     * jakarta websocket session id(channelId)── 同节点定位具体连接用。
     */
    private String channelId;

    /**
     * 接入建立时间(毫秒戳)。
     */
    private Long connectTime;

    /**
     * 最近活跃时间(毫秒戳)── 心跳续期时刷新。
     */
    private Long lastActiveTime;
}
