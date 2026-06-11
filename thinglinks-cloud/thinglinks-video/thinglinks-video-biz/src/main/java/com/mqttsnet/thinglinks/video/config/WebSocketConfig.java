package com.mqttsnet.thinglinks.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Description:
 * WebSocket 配置类，启用 Jakarta WebSocket 端点自动注册。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-02
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpoint() {
        return new ServerEndpointExporter();
    }
}
