package com.mqttsnet.thinglinks.cacert.vo.result.license;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CA 证书影响面 ── 吊销 / 评估前展示有多少设备绑定此 CA,其中多少在线。
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "CaCertLicenseImpactResultVO", description = "CA 证书影响面")
public class CaCertLicenseImpactResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "CA 证书 ID")
    private Long caId;

    @Schema(description = "CA 序列号")
    private String caSerialNumber;

    @Schema(description = "CA 证书名")
    private String caName;

    @Schema(description = "绑定此 CA 的设备总数")
    private Long boundDeviceCount;

    @Schema(description = "其中在线设备数")
    private Long onlineDeviceCount;

    /** 前 N 条设备简要列表(用于运维抽样查看) */
    @Schema(description = "Top N 设备简要列表")
    private List<Map<String, Object>> topDevices;
}
