package com.mqttsnet.thinglinks.video.media.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 * 流媒体 REST 客户端配置。
 *
 * <p>支持通过 Nacos/application.yml 配置重试策略：</p>
 * <pre>{@code
 * media:
 *   media-rest:
 *     connect-timeout: 5000       # 连接超时（毫秒）
 *     read-timeout: 10000         # 读取超时（毫秒）
 *     retry-max-attempts: 3       # 最大重试次数（不含首次请求）
 *     retry-initial-interval: 1000 # 首次重试间隔（毫秒）
 *     retry-multiplier: 2.0       # 退避倍率（指数退避，上限60秒）
 * }</pre>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "media.media-rest")
public class MediaRestConfig {

    /**
     * 连接超时（毫秒），默认 5000
     */
    private int connectTimeout = 5000;

    /**
     * 读取超时（毫秒），默认 10000
     */
    private int readTimeout = 10000;

    /**
     * 最大重试次数（不含首次请求），默认 3
     */
    private int retryMaxAttempts = 3;

    /**
     * 首次重试间隔（毫秒），默认 1000
     */
    private long retryInitialInterval = 1000L;

    /**
     * 退避倍率，默认 2.0
     */
    private double retryMultiplier = 2.0;
}
