package com.mqttsnet.thinglinks.video.config.gb28181;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * SIP 服务器全局配置（仅保留物理层参数）。
 * <p>
 * domain/id/password 已移到租户级 video_sip_config 表，
 * ip/showIp/monitorIps 由 SipLayer 自动扫描网卡获取，
 * ptzSpeed/alarm 使用代码默认值。
 *
 * @author mqttsnet
 */
@Component
@ConfigurationProperties(prefix = "sip", ignoreInvalidFields = true)
@Order(0)
@Data
public class SipConfig {

    /**
     * SIP 监听端口（全局共享）
     */
    private Integer port = 5060;

    /**
     * 默认注册有效期（秒），租户可在 video_sip_config.register_interval 中覆盖
     */
    private Integer registerTimeInterval = 60;

    /**
     * 命令发送等待回复的超时时间（毫秒）
     */
    private Long timeout = 5000L;
}
