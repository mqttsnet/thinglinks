package com.mqttsnet.thinglinks.msg.ws;

import cn.hutool.core.util.StrUtil;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 前端 WebSocket 联调入口 ── 被以下两处调用,<b>不要随意删除</b>:
 * <ul>
 *   <li>前端 demo 模块:{@code views/demo/feat/ws/index.vue}(框架自带 ws 测试 demo)</li>
 *   <li>前端 IoT 业务:{@code views/iot/link/operationMaintenance/debug/webSocket/index.vue}
 *       (运维 → 调试 → WebSocket 调试入口)</li>
 * </ul>
 * 完整调用 URL:{@code ws://{gateway-host}/api/wsMsg/anno/test}
 *
 * @author mqttsnet
 * @date 2021/8/4 23:47
 */
@ServerEndpoint("/anno/test")
@Component
@Slf4j
public class TestEndpoint {

    /**
     * 连接成功
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
        log.info("连接成功");
        WebSocketSubject subject = WebSocketSubject.Holder.getSubject(session.getId());
        subject.registerSession(session);
    }

    /**
     * 连接关闭
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        log.info("连接关闭");
        WebSocketSubject subject = WebSocketSubject.Holder.getSubject(session.getId());
        subject.unregisterSession(session);

        // close session and close Web Socket connection
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("close web socket session error.", e);
        }
    }

    /**
     * 接收到消息
     *
     * @param text
     */
    @OnMessage
    public String onMsg(@PathParam("principal") String principal, String text) {
        if (StrUtil.isEmpty(text)) {
            return "";
        }
        return "server 收到消息：" + text;
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("连接error");
        throw new RuntimeException("web socket error.", error);
    }
}
