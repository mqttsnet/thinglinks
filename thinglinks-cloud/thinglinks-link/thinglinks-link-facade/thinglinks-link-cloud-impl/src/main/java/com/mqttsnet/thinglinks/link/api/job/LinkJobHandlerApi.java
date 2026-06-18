package com.mqttsnet.thinglinks.link.api.job;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.link.api.job.hystrix.LinkJobHandlerApiFallback;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradeTasksResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * -----------------------------------------------------------------------------
 * File Name: LinkJobHandlerApi.java
 * -----------------------------------------------------------------------------
 * Description:
 * Link 服务调度任务相关API
 * 不需要登录
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2024-07-20 13:20
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:thinglinks-link-server}",
        fallback = LinkJobHandlerApiFallback.class, path = "/")
public interface LinkJobHandlerApi {


    /**
     * Refreshes the cache with device data for a specific tenant.
     *
     * @param tenantId Identifier of the tenant whose device cache needs to be refreshed.
     * @return Response indicating the result of the cache refresh operation.
     */
    @Operation(summary = "刷新设备缓存", description = "Refreshes the cache with device data for a specific tenant.")
    @Parameters({
            @Parameter(name = "tenantId", description = "Tenant ID", required = true)
    })
    @PostMapping("/anyUser/cache/refreshDeviceCache")
    R<?> refreshDeviceCacheForTenant(@RequestParam("tenantId") Long tenantId);


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
    @PostMapping("/anyUser/deviceSync/syncDeviceConnectionStatus")
    R<?> syncDeviceConnectionStatus(@RequestParam("tenantId") Long tenantId);

    /**
     * Refreshes the product cache for a specific tenant.
     *
     * @param tenantId Identifier of the tenant whose product cache needs to be refreshed.
     * @return Response indicating the result of the cache refresh operation.
     */
    @Operation(summary = "刷新产品缓存", description = "Refreshes the product cache for a specific tenant.")
    @Parameters({
            @Parameter(name = "tenantId", description = "Tenant ID", required = true)
    })
    @PostMapping("/anyUser/cache/refreshProductCache")
    R<?> refreshProductCacheForTenant(@RequestParam("tenantId") Long tenantId);


    /**
     * Refreshes the product model cache for a specific tenant.
     *
     * @param tenantId Identifier of the tenant whose product model cache needs to be refreshed.
     * @return Response indicating the result of the cache refresh operation.
     */
    @Operation(summary = "刷新产品模型缓存", description = "Refreshes the product model cache for a specific tenant.")
    @Parameters({
            @Parameter(name = "tenantId", description = "Tenant ID", required = true)
    })
    @PostMapping("/anyUser/cache/refreshProductModelCache")
    R<?> refreshProductModelCache(@RequestParam("tenantId") Long tenantId);


    @Operation(summary = "产品版本发布重试兜底", description = "Retries stuck product-version publish records for a specific tenant.")
    @Parameters({
            @Parameter(name = "tenantId", description = "Tenant ID", required = true)
    })
    @PostMapping("/anyUser/cache/retryProductVersionPublish")
    R<?> retryProductVersionPublish(@RequestParam("tenantId") Long tenantId);


    @Operation(summary = "执行OTA升级任务", description = "Executes the OTA upgrade tasks for a specific tenant.")
    @Parameters({
            @Parameter(name = "tenantId", description = "Tenant ID", required = true)
    })
    @PostMapping("/anyUser/otaOpen/otaUpgradeTasksExecute")
    R<?> otaUpgradeTasksExecute(@RequestParam("tenantId") Long tenantId);


    /**
     * 刷新指定租户的ACL规则缓存
     *
     * <p>此接口通过Feign客户端调用，用于分布式系统中跨服务刷新ACL规则缓存。</p>
     *
     * @param tenantId 需要刷新缓存的租户ID
     * @return 操作结果响应
     */
    @Operation(summary = "刷新ACL规则缓存", description = "刷新指定租户的ACL规则缓存")
    @Parameters({
            @Parameter(name = "tenantId", description = "租户ID", required = true)
    })
    @PostMapping("/anyUser/cache/refreshAclRuleCache")
    R<?> refreshAclRuleCache(@RequestParam("tenantId") Long tenantId);
}
