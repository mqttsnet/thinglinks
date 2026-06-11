package com.mqttsnet.thinglinks.video.cache;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SIP 服务信息缓存对象
 * <p>
 * 存储在 Redis 中，供集群部署时各实例共享 SIP 节点信息。
 * 每个 video-server 实例启动 SipLayer 后，将自身的 SIP 监听信息写入 Redis，
 * 其他实例可通过此缓存查找目标 SIP 节点。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "SIP 服务信息缓存对象")
public class SipServerInfoCacheVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 实例唯一标识（如 IP:PORT 或 UUID）
     */
    @Schema(description = "实例唯一标识")
    private String instanceId;

    /**
     * SIP 监听 IP 列表
     */
    @Schema(description = "SIP 监听 IP 列表")
    private List<String> monitorIps;

    /**
     * SIP 监听端口
     */
    @Schema(description = "SIP 监听端口")
    private Integer port;

    /**
     * SIP 域
     */
    @Schema(description = "SIP 域")
    private String domain;

    /**
     * SIP 服务器编号（设备端 GB28181 配置里的「SIP 服务器编号」值）。
     */
    @Schema(description = "SIP 服务器编号")
    private String sipId;

    /**
     * 显示 IP
     */
    @Schema(description = "显示 IP")
    private String showIp;

    /**
     * 在线状态
     */
    @Schema(description = "在线状态")
    private Boolean onlineStatus;

    /**
     * 注册/续期时间
     */
    @Schema(description = "注册时间")
    private LocalDateTime registerTime;
}
