package com.mqttsnet.thinglinks.video.jt1078.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description:
 * JT/T 1078 模块配置属性。
 * 通过 {@code thinglinks.video.jt1078.*} 前缀进行配置。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Component
@ConfigurationProperties(prefix = "thinglinks.video.jt1078")
public class Jt1078Config {

    /**
     * 是否启用 JT/T 1078 模块
     */
    private Boolean enabled = Boolean.FALSE;

    /**
     * JT808 网关 IP 地址
     */
    private String gatewayIp;

    /**
     * JT808 网关端口
     */
    private Integer gatewayPort;

    /**
     * 是否使用 Mock 网关（开发测试用）
     */
    private Boolean mockGateway = Boolean.TRUE;
}
