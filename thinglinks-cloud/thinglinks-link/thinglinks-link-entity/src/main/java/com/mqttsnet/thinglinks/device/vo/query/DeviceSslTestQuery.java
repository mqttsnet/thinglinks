package com.mqttsnet.thinglinks.device.vo.query;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSL 证书认证测试器请求 ── 给运维端测试器页面用,不参与设备主认证流程。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "DeviceSslTestQuery", description = "SSL 证书认证测试请求")
public class DeviceSslTestQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 客户端证书 Base64;必填 */
    @Schema(description = "客户端证书 Base64", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "clientCertBase64 不能为空")
    private String clientCertBase64;

    /** 目标 CA 序列号;留空则尝试用 clientId 关联设备的绑定 CA */
    @Schema(description = "目标 CA 序列号(可空,空则按 clientId 反查设备绑定 CA)")
    private String caSerialNumber;

    /** 客户端标识;仅用于关联设备 + 日志 */
    @Schema(description = "客户端标识(可空,仅用于反查与审计)")
    private String clientIdentifier;
}
