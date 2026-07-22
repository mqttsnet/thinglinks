package com.mqttsnet.thinglinks.msg.ws;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

/**
 * 站内信 WebSocket 会话表(按 principal 聚合该用户的所有 ws session)。
 *
 * <p>直接持有 {@link Session} 列表:{@link #notify} 遍历推送。
 * 同一 principal(用户)多端登录时 sessions 内可能多个,推送时全发。
 *
 * @author mqttsnet
 * @date 2021/8/5 14:59
 */
@Slf4j
public class WebSocketSubject {

    /**
     * subject键值
     */
    private final String principal;

    /**
     * 本 subject(同 principal)持有的所有 ws session。
     */
    private final List<Session> sessions = new CopyOnWriteArrayList<>();

    private WebSocketSubject(String principal) {
        this.principal = principal;
    }

    public String getPrincipal() {
        return principal;
    }

    /**
     * 注册 session ── @OnOpen 时调用。
     */
    public void registerSession(Session session) {
        if (session != null) {
            sessions.add(session);
        }
    }

    /**
     * 注销 session ── @OnClose 时调用。
     */
    public void unregisterSession(Session session) {
        if (session != null) {
            sessions.remove(session);
        }
    }

    /**
     * 通知客户端
     * <p>
     * 如:管理员张三发送消息给李四和王五,ws服务器先推送类型=1的消息给李四和王五,
     * 李四和王五(ws客户端)收到类型为1的消息,主动向服务器拉取消息,
     * ws服务端收到主动拉取消息,会查询数据后,推送消息类型=2的消息给李四和王五,
     * 李四和王五(ws客户端)接收到类型为2的消息,将数据解析后展示在UI页面。
     *
     * @param type 消息类型 1-通知客户端可以拉取数据了 2-通知客户端本次推送数据给你了
     * @param data 数据
     */
    public void notify(String type, String data) {
        Map<String, String> map = MapUtil.newHashMap();
        map.put("type", type);
        map.put("data", data);
        String json = JsonUtil.toJson(map);
        for (Session s : sessions) {
            if (s == null || !s.isOpen()) {
                continue;
            }
            try {
                s.getBasicRemote().sendText(json);
            } catch (IOException e) {
                // 站内信为 best-effort 提醒(客户端也会主动拉取),单个 session 发送失败不影响其它
                log.warn("[ws-msg] sendText failed sessionId={} cause={}", s.getId(), e.getMessage());
            }
        }
    }

    public static class Holder {
        private static final Map<String, WebSocketSubject> SUBJECTS = new ConcurrentHashMap<>();

        public static Map<String, WebSocketSubject> getSubject() {
            return SUBJECTS;
        }

        public static WebSocketSubject getSubject(Object principal) {
            ArgumentAssert.notNull(principal, "principal 不能为空");
            if (SUBJECTS.containsKey(principal.toString())) {
                return SUBJECTS.get(principal.toString());
            }

            WebSocketSubject subject = new WebSocketSubject(principal.toString());
            SUBJECTS.put(principal.toString(), subject);
            return subject;
        }
    }

}
