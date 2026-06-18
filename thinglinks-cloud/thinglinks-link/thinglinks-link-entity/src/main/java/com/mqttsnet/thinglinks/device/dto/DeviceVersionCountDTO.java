package com.mqttsnet.thinglinks.device.dto;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 按"设备绑定版本"分组的设备计数行 ── countDevicesGroupByBoundVersion 的结果元素。
 * versionNo 取设备 bound_product_version_no(未绑定的设备归到空串 "")。
 *
 * @author mqttsnet
 */
@Data
@Schema(title = "DeviceVersionCountDTO", description = "按绑定版本分组的设备计数")
public class DeviceVersionCountDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备绑定版本号(未绑定归到空串)")
    private String versionNo;

    @Schema(description = "该版本下的设备数")
    private Long deviceCount;
}
