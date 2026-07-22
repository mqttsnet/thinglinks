package com.mqttsnet.thinglinks.broker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置 ── 启用 Jakarta {@code @ServerEndpoint} 自动注册。
 *
 * <p><b>必须放在 broker-server(主应用模块,挨着 {@code BrokerServerApplication})。</b>
 * {@link ServerEndpointExporter} 要和嵌入式 Servlet 容器处于同一应用上下文,容器就绪后才能扫描并发布
 * broker-controller 下的 {@code @ServerEndpoint}(如 {@code WebSocketDeviceOpenAccessProtocolEndpoint});
 * 放到 biz 等库模块曾导致 Undertow 下端点注册不上、握手 404。
 *
 * @author mqttsnet
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpoint() {
        return new ServerEndpointExporter();
    }
}

