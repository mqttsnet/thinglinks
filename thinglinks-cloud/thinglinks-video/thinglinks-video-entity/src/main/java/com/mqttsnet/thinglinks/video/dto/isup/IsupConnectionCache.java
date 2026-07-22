package com.mqttsnet.thinglinks.video.dto.isup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ISUP 设备连接缓存对象。
 * <p>
 * IsupConnectionInfo (record) 的可序列化 POJO 镜像，用于 Redis 存储。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsupConnectionCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String deviceSerial;

    private Integer userId;

    private Integer channelCount;

    private String ip;

    private Integer port;

    private LocalDateTime loginTime;
}
