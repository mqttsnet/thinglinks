package com.mqttsnet.thinglinks.video.dto.onvif;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * WS-Discovery 探测到的 ONVIF 设备摘要。
 *
 * <p>仅承载从 ProbeMatch 直接得到的字段——更详细的信息（厂商 / 型号 / 序列号 / Profile 列表）
 * 需要后续 SOAP 调用 {@code GetDeviceInformation} / {@code GetProfiles} 获取，
 * 这一层不嵌入避免每次发现都触发 N+1 调用。
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "WS-Discovery 探测结果")
public class OnvifDevice implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 设备 SOAP 端点 URL，如 {@code http://192.168.1.108/onvif/device_service}。 */
    @Schema(description = "设备 SOAP 端点 URL")
    private String xaddr;

    /**
     * 设备 EndpointReference，全网唯一 UUID（设备出厂时固化）。
     * 同一设备多次扫描这个值不变，可以做"已注册过滤"。
     */
    @Schema(description = "设备唯一 UUID")
    private String endpointReference;

    /** 设备类型字段（如 {@code dn:NetworkVideoTransmitter}）。 */
    @Schema(description = "设备类型")
    private String types;

    /** 设备范围 / 元信息列表（如制造商、硬件信息），原始字符串保留。 */
    @Schema(description = "Scope 列表")
    private List<String> scopes;
}
