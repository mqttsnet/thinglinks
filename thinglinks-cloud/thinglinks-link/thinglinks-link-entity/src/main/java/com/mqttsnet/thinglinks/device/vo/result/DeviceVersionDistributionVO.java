package com.mqttsnet.thinglinks.device.vo.result;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 产品下设备按"绑定版本"的实时分布 ── 供发布管理 / 版本列表展示各版本"当前铺开了多少台、占比多少"。
 * total = 该产品设备总数(含未绑定);versionCounts = 版本号 → 设备数(未绑定归到空串 "")。
 *
 * @author mqttsnet
 */
@Data
@Schema(title = "DeviceVersionDistributionVO", description = "产品下设备按绑定版本的实时分布")
public class DeviceVersionDistributionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "该产品设备总数(含未绑定版本的设备)")
    private Long total;

    @Schema(description = "各版本设备数:版本号 → 设备数(未绑定归到空串)")
    private Map<String, Long> versionCounts;
}
