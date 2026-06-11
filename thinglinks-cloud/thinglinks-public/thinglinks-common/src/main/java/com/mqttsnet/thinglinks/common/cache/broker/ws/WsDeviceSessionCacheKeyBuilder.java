package com.mqttsnet.thinglinks.common.cache.broker.ws;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * WS 设备 session 全量信息缓存 KEY。
 *
 * <h3>用途</h3>
 * 记录 {@code clientId → 设备连接渠道信息(JSON,见 WsDeviceSessionInfo)},供<b>多节点共享读取</b>:
 * 任一 broker / 业务节点都能据此判断设备是否在线、连在哪个节点、走什么渠道。
 *
 * <h3>不用于下行路由</h3>
 * 下行命令走 RocketMQ 广播(各节点查本地 session 投递),不依赖此 key 里的节点地址做点对点转发。
 * 取代了原"owner 路由"(存 ip:port + RestTemplate 反向调用)的脆弱方案。
 *
 * <h3>生命周期</h3>
 * <ul>
 *   <li>设备 ws @OnOpen → broker 写入:{@code SET key=WsDeviceSessionInfo(JSON) EX 90s}</li>
 *   <li>设备发心跳 PING → broker 续期 TTL(刷新 lastActiveTime)</li>
 *   <li>设备 @OnClose / 心跳超时 → broker 删除 key</li>
 *   <li>设备掉线但 broker 未感知 → 90s 后 TTL 自然过期,后续 publishMessage 查不到 → 视为"设备不在线"</li>
 * </ul>
 *
 * <h3>key 格式</h3>
 * <pre>
 * {prefix}:{tenantId}:broker:def_ws_session:id:obj:{clientId} → WsDeviceSessionInfo(对象,带 @class)
 * </pre>
 *
 * @author mqttsnet
 */
public class WsDeviceSessionCacheKeyBuilder implements CacheKeyBuilder {

    private Long tenantId;

    /**
     * @param clientId 设备 ws 接入 clientId
     * @return {@link CacheKey} key (含 90s TTL)
     */
    public static CacheKey build(String clientId) {
        return new WsDeviceSessionCacheKeyBuilder()
                .setTenantId(ContextUtil.getTenantId())
                .key(clientId);
    }

    /**
     * 显式指定 tenantId 构造(broker 解析 ws 子协议时已拿到 tenantId,不依赖 ContextUtil)。
     */
    public static CacheKey build(Long tenantId, String clientId) {
        return new WsDeviceSessionCacheKeyBuilder()
                .setTenantId(tenantId)
                .key(clientId);
    }

    @Override
    public String getTenant() {
        return String.valueOf(this.tenantId);
    }

    @Override
    public WsDeviceSessionCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Broker.WS_SESSION;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.BROKER;
    }

    @Override
    public String getField() {
        // 业务字段 = clientId(每条 session 信息的唯一标识)
        return "id";
    }

    @Override
    public ValueType getValueType() {
        // 存 WsDeviceSessionInfo 对象(标准对象缓存,与 DeviceCacheVO 一致)
        return ValueType.obj;
    }

    /**
     * TTL 跟心跳超时一致(90s)。
     * <p>设备每 30s 发一次 PING → broker SET 续期;90s 内无 PING → 自然过期,视为离线。
     */
    @Override
    public Duration getExpire() {
        return Duration.ofSeconds(90);
    }
}
