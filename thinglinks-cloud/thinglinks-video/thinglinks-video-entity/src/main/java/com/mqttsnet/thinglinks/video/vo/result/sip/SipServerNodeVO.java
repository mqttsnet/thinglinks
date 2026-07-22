package com.mqttsnet.thinglinks.video.vo.result.sip;

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
 * SIP 服务节点信息 VO
 * <p>
 * 展示集群中每个 SIP 服务实例的状态信息。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "SIP 服务节点信息")
public class SipServerNodeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "实例 ID（IP:PORT）")
    private String instanceId;

    @Schema(description = "监听 IP 列表")
    private List<String> monitorIps;

    @Schema(description = "监听端口")
    private Integer port;

    @Schema(description = "在线状态")
    private Boolean onlineStatus;

    @Schema(description = "注册/续期时间")
    private LocalDateTime registerTime;
}
