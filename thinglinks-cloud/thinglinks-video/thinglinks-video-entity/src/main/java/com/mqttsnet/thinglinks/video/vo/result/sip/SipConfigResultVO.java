package com.mqttsnet.thinglinks.video.vo.result.sip;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SIP 接入配置信息返回 VO
 * <p>
 * 用于 Web 页面展示 SIP 信令服务配置信息，方便用户配置 GB28181 设备接入。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "SIP 接入配置信息")
public class SipConfigResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "SIP 服务器 ID（设备端填写的 SIP 服务器 ID，20位国标编码）")
    private String sipId;

    @Schema(description = "SIP 域（设备端填写的 SIP 域，通常为国标编码前10位）")
    private String sipDomain;

    @Schema(description = "SIP 服务端口")
    private Integer sipPort;

    @Schema(description = "SIP 认证密码")
    private String sipPassword;

    @Schema(description = "SIP 服务地址（设备端填写的 SIP 服务器地址）")
    private String sipHost;

    @Schema(description = "注册有效期（秒）")
    private Integer registerTimeInterval;

    @Schema(description = "SIP 服务节点列表（集群模式下的多个活跃节点）")
    private List<SipServerNodeVO> serverNodes;
}
