package com.mqttsnet.thinglinks.video.dto.jt1078;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JT/T 1078 终端连接缓存对象。
 * <p>
 * Jt1078ConnectionInfo (record) 的可序列化 POJO 镜像，用于 Redis 存储。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Jt1078ConnectionCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String simNumber;

    private String plateNumber;

    private Integer channelCount;

    private String ip;

    private Integer port;

    private LocalDateTime loginTime;
}
