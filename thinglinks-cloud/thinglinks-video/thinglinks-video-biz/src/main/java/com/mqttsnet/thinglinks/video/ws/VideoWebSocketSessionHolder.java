package com.mqttsnet.thinglinks.video.ws;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.basic.jackson.JsonUtil;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Description:
 * WebSocket 会话持有器（video-biz 层，供 Endpoint 和 Pusher 共享）。
 * <p>
 * 解决 Endpoint 在 video-controller、Pusher 在 video-biz 的跨模块引用问题。
 * 两者都引用此类（在 video-biz 层），避免循环依赖。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-09
 */
@Slf4j
public class VideoWebSocketSessionHolder {

    /** 设备状态频道: tenantId → sessions */
    private static final Map<String, Set<Session>> DEVICE_STATUS_SESSIONS = new ConcurrentHashMap<>();

    /** 告警推送频道: tenantId → sessions */
    private static final Map<String, Set<Session>> ALARM_SESSIONS = new ConcurrentHashMap<>();

    // ===== 设备状态频道 =====

    public static void addDeviceStatusSession(String tenantId, Session session) {
        DEVICE_STATUS_SESSIONS.computeIfAbsent(tenantId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public static void removeDeviceStatusSession(String tenantId, Session session) {
        removeSession(DEVICE_STATUS_SESSIONS, tenantId, session);
    }

    public static void pushDeviceStatus(String tenantId, Object message) {
        pushToTenant(DEVICE_STATUS_SESSIONS, tenantId, message, "设备状态");
    }

    // ===== 告警推送频道 =====

    public static void addAlarmSession(String tenantId, Session session) {
        ALARM_SESSIONS.computeIfAbsent(tenantId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public static void removeAlarmSession(String tenantId, Session session) {
        removeSession(ALARM_SESSIONS, tenantId, session);
    }

    public static void pushAlarm(String tenantId, Object message) {
        pushToTenant(ALARM_SESSIONS, tenantId, message, "告警");
    }

    // ===== 通用方法 =====

    private static void pushToTenant(Map<String, Set<Session>> sessionMap, String tenantId, Object message, String channel) {
        Set<Session> sessions = sessionMap.get(tenantId);
        if (CollUtil.isEmpty(sessions)) {
            return;
        }
        String json = JsonUtil.toJson(message);
        sessions.removeIf(s -> !s.isOpen());
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(json);
            } catch (IOException e) {
                log.debug("[WS-{}] 推送失败: sessionId={}, error={}", channel, session.getId(), e.getMessage());
            }
        }
    }

    private static void removeSession(Map<String, Set<Session>> sessionMap, String tenantId, Session session) {
        Set<Session> sessions = sessionMap.get(tenantId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionMap.remove(tenantId);
            }
        }
    }
}
