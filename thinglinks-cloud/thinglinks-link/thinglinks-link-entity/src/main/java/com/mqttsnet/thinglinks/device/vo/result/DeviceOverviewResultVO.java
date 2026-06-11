package com.mqttsnet.thinglinks.device.vo.result;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * @program: thinglinks-cloud
 * @description: 设备概况统计
 * @packagename: com.mqttsnet.thinglinks.device.vo.result
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-13 09:28
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "DeviceOverviewResultVO", description = "设备概况统计")
public class DeviceOverviewResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备总量")
    private Integer totalDevicesCount;

    @Schema(description = "未连接量")
    private Integer notConnectedCount;

    @Schema(description = "在线量")
    private Integer onlineCount;

    @Schema(description = "离线量")
    private Integer offlineCount;

    @Schema(description = "未激活量")
    private Integer notActivatedCount;

    @Schema(description = "已激活量")
    private Integer activatedCount;

    @Schema(description = "已锁定量")
    private Integer lockedCount;

    @Schema(description = "网关设备量")
    private Integer gatewayDeviceCount;

    @Schema(description = "普通设备量")
    private Integer ordinaryCount;

    @Schema(description = "子设备量")
    private Integer subDeviceCount;

    @Schema(description = "今日新增设备量")
    private Integer todayNewCount;

    @Schema(description = "近7天新增设备量")
    private Integer weekNewCount;

    @Schema(description = "近30天新增设备量")
    private Integer monthNewCount;
}
