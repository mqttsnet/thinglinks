package com.mqttsnet.thinglinks.anyuser.controller;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.cache.device.DeviceCacheService;
import com.mqttsnet.thinglinks.cache.product.ProductCacheService;
import com.mqttsnet.thinglinks.cache.product.ProductModelCacheService;
import com.mqttsnet.thinglinks.productversion.publish.orchestrator.ProductVersionPublishOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * -----------------------------------------------------------------------------
 * File Name: DeviceCacheController
 * -----------------------------------------------------------------------------
 * Description:
 * 缓存相关 API
 * 不需要登录 注意设置：ContextUtil.setTenantId(tenantId);
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/7/20       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/7/20 19:29
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/anyUser/cache")
@Tag(name = "anyUser-缓存相关API")
public class CacheAnyUserController {

    private final DeviceCacheService deviceCacheService;

    private final ProductCacheService productCacheService;

    private final ProductModelCacheService productModelCacheService;

    private final ProductVersionPublishOrchestrator productVersionPublishOrchestrator;


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
    @PostMapping("/refreshDeviceCache")
    public R<?> refreshDeviceCacheForTenant(@RequestParam("tenantId") Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId Cannot be null");
        log.info("Refreshing device cache for tenant ID: {}", tenantId);
        ContextUtil.setTenantId(tenantId);
        deviceCacheService.refreshDeviceCacheForTenant(tenantId);
        return R.success();
    }

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
    @PostMapping("/refreshProductCache")
    public R<?> refreshProductCacheForTenant(@RequestParam("tenantId") Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId Cannot be null");
        log.info("Refreshing product cache for tenant ID: {}", tenantId);
        ContextUtil.setTenantId(tenantId);
        productCacheService.refreshProductCacheForTenant(tenantId);
        return R.success();
    }


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
    @PostMapping("/refreshProductModelCache")
    public R<?> refreshProductModelCache(@RequestParam("tenantId") Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId Cannot be null");
        log.info("Refreshing product model cache for tenant ID: {}", tenantId);
        ContextUtil.setTenantId(tenantId);
        productModelCacheService.refreshAllProductModelCacheForTenant(tenantId);
        return R.success();
    }

    /**
     * 产品版本发布重试兜底(独立于缓存预热,微服务模式经此端点触发,补齐 cloud 部署下的发布兜底覆盖)。
     *
     * @param tenantId 租户 ID
     * @return 执行结果
     */
    @Operation(summary = "产品版本发布重试兜底", description = "Retries stuck product-version publish records for a specific tenant.")
    @Parameters({
            @Parameter(name = "tenantId", description = "Tenant ID", required = true)
    })
    @PostMapping("/retryProductVersionPublish")
    public R<?> retryProductVersionPublish(@RequestParam("tenantId") Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId Cannot be null");
        ContextUtil.setTenantId(tenantId);
        int retried = productVersionPublishOrchestrator.retryRunningRecordsForTenant(tenantId);
        log.info("[publish-retry-job] tenant={} retried={}", tenantId, retried);
        return R.success();
    }

}
