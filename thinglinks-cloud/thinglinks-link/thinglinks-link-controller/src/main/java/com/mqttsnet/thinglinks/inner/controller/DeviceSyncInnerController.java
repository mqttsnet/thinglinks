package com.mqttsnet.thinglinks.inner.controller;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.device.service.DeviceSyncInnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备信息同步相关内部接口（inner）
 * Feign 服务间 RPC(Nacos 直连、不过网关)：透传 TenantId、无需 Token；网关拒绝外部访问。
 *
 * @author mqttsnet
 * @date 2024-06-30
 * @create [2024-06-30] [mqttsnet]
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/inner/deviceSync")
@Tag(name = "inner-设备数据同步相关API")
public class DeviceSyncInnerController {

    @Autowired
    private DeviceSyncInnerService deviceSyncInnerService;

    /**
     * 同步设备连接状态
     *
     * @param tenantId 租户ID
     * @return 操作结果
     */
    @Operation(summary = "同步设备连接状态", description = "同步指定租户下所有网关设备和直连设备的连接状态。")
    @Parameters({
            @Parameter(name = "tenantId", description = "租户ID", required = true)
    })
    @PostMapping("/syncDeviceConnectionStatus")
    public R<?> syncDeviceConnectionStatus(@RequestParam("tenantId") Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId cannot be null");
        log.info("Starting device connection status sync for tenantId: {}", tenantId);
        deviceSyncInnerService.syncDeviceConnectionStatus(tenantId);
        return R.success();
    }


}
