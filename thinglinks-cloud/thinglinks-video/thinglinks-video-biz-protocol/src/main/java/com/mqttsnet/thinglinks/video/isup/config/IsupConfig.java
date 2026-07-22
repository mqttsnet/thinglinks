package com.mqttsnet.thinglinks.video.isup.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description:
 * ISUP 模块配置。
 * 通过 {@code thinglinks.video.isup} 前缀读取 YAML/Properties 配置。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Component
@ConfigurationProperties(prefix = "thinglinks.video.isup")
public class IsupConfig {

    /**
     * 是否启用 ISUP 模块
     */
    private Boolean enabled = false;

    /**
     * ISUP 监听 IP
     */
    private String listenIp = "0.0.0.0";

    /**
     * ISUP 监听端口
     */
    private Integer listenPort = 7660;

    /**
     * SDK 库文件路径
     */
    private String sdkPath;

    /**
     * 是否使用 Mock SDK（开发测试环境）
     */
    private Boolean mockSdk = true;
}
