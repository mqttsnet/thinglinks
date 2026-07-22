package com.mqttsnet.thinglinks.broker.ws.session;


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mqttsnet.basic.utils.ArgumentAssert;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;

/**
 * WS 设备会话表(按 clientId 聚合该设备的所有本地 ws session)。
 *
 * <p>broker 微服务作为协议接入层持有 ws session;mqs 微服务不持有 socket / session,
 * 纯消费 Kafka 做业务处理(DeviceAction 持久化、物模型解析、桥接旁路)。
 *
 * <p>直接持有 {@link Session} 列表:下行命令 {@link #notify} 遍历发送、心跳超时 {@link #closeAllSessions}
 * 主动关连接(close 后 jakarta.websocket 自动回调 @OnClose,触发 endpoint 清理 + Kafka 投 disconnect 事件)。
 * 同一 clientId 多次连接时(如设备重连未 close 旧链接),sessions 内可能多个;close 时全关。
 *
 * @author mqttsnet
 * @date 2024/9/20 14:59
 */
public class WebSocketSubject {

    /**
     * subject键值
     */
    private final String principal;

    /**
     * 最后活跃时间(毫秒戳)。
     * <p>设备每次上行报文时由 {@code WsHeartbeatTracker.update} 更新;
     * {@code WsHeartbeatTimeoutChecker} 定期扫,超过 90s 未活跃则触发 disconnect。
     * <p>跨节点重连漂移场景下,{@code WsHeartbeatSyncListener} 收到广播后也会更新。
     * <p>{@code volatile} 保证多线程可见性(Tracker / Checker / Listener 不同线程访问)。
     */
    private volatile long lastActiveTime;

    /**
     * 本 subject 持有的所有 ws session(集群部署下需主动 close socket 触发 @OnClose)。
     * <p>用 {@link CopyOnWriteArrayList} 因为读多写少(写=connect/close,读=发送/close all)。
     */
    private final List<Session> sessions = new CopyOnWriteArrayList<>();

    private WebSocketSubject(String principal) {
        this.principal = principal;
        this.lastActiveTime = System.currentTimeMillis();
    }

    public String getPrincipal() {
        return principal;
    }

    /**
     * 获取最后活跃时间(毫秒)。
     */
    public long getLastActiveTime() {
        return lastActiveTime;
    }

    /**
     * 设置最后活跃时间。Tracker / SyncListener / Checker 都会调用。
     * <p>用 max(current, ts) 防止"广播事件迟到导致回退",保证时间戳单调递增。
     */
    public void touchLastActiveTime(long ts) {
        if (ts > this.lastActiveTime) {
            this.lastActiveTime = ts;
        }
    }

    /**
     * 注册 session ── @OnOpen 时调用。
     */
    public void registerSession(Session session) {
        if (session == null) {
            return;
        }
        sessions.add(session);
    }

    /**
     * 注销 session ── @OnClose 时调用。
     */
    public void unregisterSession(Session session) {
        if (session == null) {
            return;
        }
        sessions.remove(session);
    }

    /**
     * 主动关闭所有 session ── 心跳超时 checker 调用。
     * <p>session.close() 会触发 jakarta.websocket 容器调用 endpoint 的 @OnClose,
     * 后者再清除 Redis session 信息 + 投 Kafka disconnect 事件,链路自洽。
     */
    public void closeAllSessions(CloseReason reason) {
        for (Session s : sessions) {
            try {
                if (s != null && s.isOpen()) {
                    s.close(reason);
                }
            } catch (IOException ignore) {
                // close 失败忽略,session 可能已断
            }
        }
        sessions.clear();
    }

    /**
     * 当前持有的 session 数(诊断用)
     */
    public int getSessionCount() {
        return sessions.size();
    }

    /**
     * 当前持有的所有 session(只读视图)── 供在线续期 / 诊断遍历。
     */
    public List<Session> getSessions() {
        return Collections.unmodifiableList(sessions);
    }

    /**
     * 下行推送 ── 把报文发给本 subject 持有的所有在线 session。
     *
     * @param rawData 报文(子协议 JSON 字符串)
     */
    public void notify(Object rawData) {
        if (rawData == null) {
            return;
        }
        String text = String.valueOf(rawData);
        for (Session s : sessions) {
            if (s == null || !s.isOpen()) {
                continue;
            }
            try {
                s.getBasicRemote().sendText(text);
            } catch (IOException e) {
                throw new RuntimeException("ws 发送消息失败", e);
            }
        }
    }

    public static class Holder {
        private static final Map<String, WebSocketSubject> subjects = new ConcurrentHashMap<>();

        public static Map<String, WebSocketSubject> getSubject() {
            return subjects;
        }

        public static WebSocketSubject getSubject(Object principal) {
            ArgumentAssert.notNull(principal, "principal 不能为空");
            if (subjects.containsKey(principal.toString())) {
                return subjects.get(principal.toString());
            }

            WebSocketSubject subject = new WebSocketSubject(principal.toString());
            subjects.put(principal.toString(), subject);
            return subject;
        }

        /**
         * 移除 session(由 @OnClose 调用),清理本地内存表。
         *
         * @param principal clientId
         * @return 被移除的 subject(若存在)
         */
        public static WebSocketSubject removeSubject(Object principal) {
            if (principal == null) {
                return null;
            }
            return subjects.remove(principal.toString());
        }
    }

}
