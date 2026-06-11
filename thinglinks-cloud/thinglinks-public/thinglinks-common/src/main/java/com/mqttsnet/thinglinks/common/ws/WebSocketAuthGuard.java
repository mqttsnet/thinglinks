package com.mqttsnet.thinglinks.common.ws;

import java.io.IOException;
import java.util.Objects;

import cn.hutool.core.util.StrUtil;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket onOpen 鉴权工具(配合 {@link WebSocketAuthHeaderCaptor})。
 * 校验失败时内部 close session(VIOLATED_POLICY) + 审计 log,业务方只看 boolean 决定 return;
 * 不抛异常 ── Tomcat 对 onOpen 抛异常行为不稳定,用关闭码更可控。
 *
 * @author mqttsnet
 */
@Slf4j
public final class WebSocketAuthGuard {

    private WebSocketAuthGuard() {
    }

    /**
     * 要求:已登录 + 路径段 tenantId 与登录 tenantId 严格相等(防跨租户越权)。
     *
     * <p>不通过时内部已 close session + 审计日志,调用方只需 if (!requireSameTenant(...)) return;</p>
     *
     * @param session      ws 会话
     * @param config       endpoint 配置(从中取 userProperties)
     * @param pathTenantId URL 路径中声明的 tenantId(@PathParam 拿到的)
     * @return 通过返回 true;否则 false 且 session 已关闭
     */
    public static boolean requireSameTenant(Session session, EndpointConfig config, String pathTenantId) {
        Object raw = config == null ? null : config.getUserProperties().get(WebSocketUserProps.LOGIN_TENANT_ID);
        String loginTenantId = raw == null ? null : raw.toString();

        if (StrUtil.isBlank(loginTenantId)) {
            denyClose(session, "未登录或网关未写入 TENANT_ID_HEADER", pathTenantId, null, config);
            return false;
        }
        if (!Objects.equals(loginTenantId, pathTenantId)) {
            denyClose(session, "tenantId 越权:路径 tenant 与登录 tenant 不一致", pathTenantId, loginTenantId, config);
            return false;
        }
        return true;
    }

    /**
     * 仅要求"已登录",不校验 tenant(用于不带 tenantId 路径段的 endpoint,或单纯校验登录态)。
     *
     * @param session ws 会话
     * @param config  endpoint 配置(从中取 userProperties)
     * @return 已登录返回 true;否则 false 且 session 已关闭
     */
    public static boolean requireLogin(Session session, EndpointConfig config) {
        Object raw = config == null ? null : config.getUserProperties().get(WebSocketUserProps.LOGIN_USER_ID);
        String loginUserId = raw == null ? null : raw.toString();
        if (StrUtil.isBlank(loginUserId)) {
            denyClose(session, "未登录或网关未写入 USER_ID_HEADER", null, null, config);
            return false;
        }
        return true;
    }

    /**
     * 取登录用户 ID(已通过 guard 校验后调用,可能为 null 由调用方兜底)。
     *
     * @param config endpoint 配置(从中取 userProperties)
     * @return 登录用户 ID;不存在返 null
     */
    public static String getLoginUserId(EndpointConfig config) {
        if (config == null) return null;
        Object v = config.getUserProperties().get(WebSocketUserProps.LOGIN_USER_ID);
        return v == null ? null : v.toString();
    }

    /**
     * 取登录用户 tenantId(已通过 guard 校验后调用,可能为 null 由调用方兜底)。
     *
     * @param config endpoint 配置(从中取 userProperties)
     * @return 登录用户 tenantId;不存在返 null
     */
    public static String getLoginTenantId(EndpointConfig config) {
        if (config == null) return null;
        Object v = config.getUserProperties().get(WebSocketUserProps.LOGIN_TENANT_ID);
        return v == null ? null : v.toString();
    }

    /**
     * 拒绝并关闭 session;审计日志带上路径 tenant / 登录 tenant / 远端 IP / UA,便于排查恶意尝试。
     * 不抛异常 ── 详见类 JavaDoc 说明。
     *
     * @param session       ws 会话
     * @param reason        拒绝原因(审计日志用)
     * @param pathTenantId  URL 路径中声明的 tenantId
     * @param loginTenantId 登录态 tenantId
     * @param config        endpoint 配置(从中取远端 IP / UA)
     */
    private static void denyClose(Session session, String reason,
                                  String pathTenantId, String loginTenantId,
                                  EndpointConfig config) {
        String sessionId = session == null ? "null" : session.getId();
        Object remoteAddr = config == null ? null
            : config.getUserProperties().get(WebSocketUserProps.REMOTE_ADDR);
        Object userAgent = config == null ? null
            : config.getUserProperties().get(WebSocketUserProps.USER_AGENT);
        log.warn("[ws-acl-deny] reason={} sessionId={} pathTenantId={} loginTenantId={} remoteAddr={} ua={}",
            reason, sessionId, pathTenantId, loginTenantId, remoteAddr, userAgent);

        if (session == null) {
            return;
        }
        try {
            if (session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "policy violation"));
            }
        } catch (IOException e) {
            log.warn("[ws-acl-deny] close session failed, sessionId={}", sessionId, e);
        }
    }
}
