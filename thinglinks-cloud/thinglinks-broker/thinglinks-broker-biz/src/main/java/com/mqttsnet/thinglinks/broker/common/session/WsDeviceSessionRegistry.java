package com.mqttsnet.thinglinks.broker.common.session;

import java.util.Optional;

import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.cache.utils.CachePlusUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.broker.ws.WsDeviceSessionCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.broker.ws.WsDeviceSessionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * broker WS 设备 session 信息注册表(Redis 实现)。
 *
 * <p>把设备整条会话的渠道信息({@link WsDeviceSessionInfo})以 JSON 存 Redis,供<b>多节点共享读取</b>:
 * 任一 broker / 业务节点都能据此判断设备是否在线、走什么渠道。
 *
 * <h3>key 的租户 ── 用传入的真实租户</h3>
 * 写(@OnOpen,租户来自连接路径段 = 当前登录租户)与读(下行在线校验,租户来自请求上下文 = 当前登录租户)
 * 都用<b>明确传入的租户</b>(同租户操作下两端一致)。<b>不从 clientId 的 {@code @} 后缀猜租户</b> ──
 * 有些设备 clientId 不遵守该规则,且跨租户可能撞 key。<b>不参与下行路由</b>(下行走 RocketMQ 广播)。
 *
 * <h3>故障容忍</h3>
 * Redis 读写失败仅 warn 不抛 ── session 信息是软状态,90s TTL 自然兜底,不应阻塞设备接入 / 心跳路径。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsDeviceSessionRegistry {

    private final CachePlusOps cachePlusOps;
    private final CachePlusUtil cachePlusOpsUtil;

    /**
     * 写入 / 续期 session 信息(SET + 重置 TTL)。
     * <p>两处调用:@OnOpen 鉴权通过后首次写入;心跳检查器每 30s 对在线会话重写续期 ── 重写即续 TTL,
     * 且能在 Redis 丢键(重启 / 驱逐)后<b>自愈重建</b>。高频路径,成功仅 debug 日志。
     *
     * @param info 设备连接渠道信息(tenantId / clientId 必填)
     */
    public void save(WsDeviceSessionInfo info) {
        if (info == null || info.getClientId() == null) {
            return;
        }
        try {
            CacheKey key = WsDeviceSessionCacheKeyBuilder.build(parseTenantIdLong(info.getTenantId()), info.getClientId());
            cachePlusOps.set(key, info);
            if (log.isDebugEnabled()) {
                log.debug("[ws-session] save tenantId={} clientId={}", info.getTenantId(), info.getClientId());
            }
        } catch (Exception e) {
            log.warn("[ws-session] save failed (non-blocking) clientId={} cause={}",
                info.getClientId(), e.getMessage());
        }
    }

    /**
     * 读设备 session 信息(多节点共享查询入口)。
     *
     * @param tenantId 租户 ID(当前登录 / 请求上下文租户)
     * @param clientId 客户端 ID
     * @return 在线则返回 session 信息;离线 / 读失败返回 empty
     */
    public Optional<WsDeviceSessionInfo> get(Long tenantId, String clientId) {
        try {
            CacheKey key = WsDeviceSessionCacheKeyBuilder.build(tenantId, clientId);
            return cachePlusOpsUtil.getObjectFromCache(key.getKey(), WsDeviceSessionInfo.class);
        } catch (Exception e) {
            log.warn("[ws-session] get failed clientId={} cause={}", clientId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 设备是否在线(session 信息是否存在)。
     */
    public boolean isOnline(Long tenantId, String clientId) {
        return get(tenantId, clientId).isPresent();
    }

    /**
     * 设备 ws @OnClose / 心跳超时时清除 session 信息。
     */
    public void remove(Long tenantId, String clientId) {
        try {
            CacheKey key = WsDeviceSessionCacheKeyBuilder.build(tenantId, clientId);
            cachePlusOps.del(key);
            log.info("[ws-session] remove tenantId={} clientId={}", tenantId, clientId);
        } catch (Exception e) {
            log.warn("[ws-session] remove failed (non-blocking) clientId={} cause={}",
                clientId, e.getMessage());
        }
    }

    /**
     * 把字符串 tenantId 转 Long;非法 → hashCode 兜底(保持 key 一致性),空 → 0L。
     */
    private Long parseTenantIdLong(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(tenantId);
        } catch (NumberFormatException e) {
            return (long) tenantId.hashCode();
        }
    }
}
