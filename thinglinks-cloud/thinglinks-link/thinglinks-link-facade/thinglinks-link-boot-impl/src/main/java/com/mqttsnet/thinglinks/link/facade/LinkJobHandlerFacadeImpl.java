package com.mqttsnet.thinglinks.link.facade;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.cache.device.DeviceCacheService;
import com.mqttsnet.thinglinks.cache.product.ProductCacheService;
import com.mqttsnet.thinglinks.cache.product.ProductModelCacheService;
import com.mqttsnet.thinglinks.device.service.DeviceSyncAnyUserService;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeTaskExecutionService;
import com.mqttsnet.thinglinks.productversion.publish.orchestrator.ProductVersionPublishOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author tangyh
 * @since 2024/12/24 17:02
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LinkJobHandlerFacadeImpl implements LinkJobHandlerFacade {
    private final DeviceCacheService deviceCacheService;
    private final ProductCacheService productCacheService;
    private final ProductModelCacheService productModelCacheService;
    private final DeviceSyncAnyUserService deviceSyncAnyUserService;
    private final OtaUpgradeTaskExecutionService otaUpgradeTaskExecutionService;
    /**
     * 发布编排器 ── 提供 retry 兜底能力。
     * 事件路径(监听器)和定时路径(本 facade 的 refreshProductModelCache)共享同一份编排逻辑,
     * 避免"事件路径修了,job 路径忘了"的逻辑漂移。
     */
    private final ProductVersionPublishOrchestrator productVersionPublishOrchestrator;


    @Override
    public R<?> refreshDeviceCacheForTenant(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId Cannot be null");
        log.info("Refreshing device cache for tenant ID: {}", tenantId);
        deviceCacheService.refreshDeviceCacheForTenant(tenantId);
        return R.success();
    }

    @Override
    public R<?> syncDeviceConnectionStatus(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId cannot be null");
        log.info("Starting device connection status sync for tenantId: {}", tenantId);
        deviceSyncAnyUserService.syncDeviceConnectionStatus(tenantId);
        return R.success();
    }

    @Override
    public R<?> refreshProductCacheForTenant(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId Cannot be null");
        log.info("Refreshing product cache for tenant ID: {}", tenantId);
        productCacheService.refreshProductCacheForTenant(tenantId);
        return R.success();
    }

    @Override
    public R<?> refreshProductModelCache(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId Cannot be null");
        // 仅做预热:按版本快照维度遍历刷新,每个产品的每个激活态版本 (PUBLISHED/CANARY/SHADOW) 都预热,
        // 避免灰度场景设备命中老版本时还要走 read-through DB IO。
        // 发布重试兜底已拆到独立 job(见 retryProductVersionPublish),与本预热解耦、故障互不影响。
        log.info("Refreshing all product model cache for tenant (all products × all active versions): {}", tenantId);
        productModelCacheService.refreshAllProductModelCacheForTenant(tenantId);
        return R.success();
    }

    @Override
    public R<?> retryProductVersionPublish(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId Cannot be null");
        // 扫描该租户卡 RUNNING / FAILED 的发布记录幂等重试:事件路径成功的已是 SUCCESS 会跳过;
        // 事件丢失 / JVM 重启 / 服务抖动卡住的在此重试;超 1h 仍 RUNNING 标 FAILED 不再重试。
        int retried = productVersionPublishOrchestrator.retryRunningRecordsForTenant(tenantId);
        if (retried > 0) {
            log.info("[link-job] retried {} publish records for tenant {}", retried, tenantId);
        }
        return R.success();
    }

    @Override
    public R<?> otaUpgradeTasksExecute(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId Cannot be null");
        log.info("Executing OTA Upgrade Tasks - Tenant ID: {}", tenantId);
        otaUpgradeTaskExecutionService.otaUpgradeTasksExecute(tenantId);
        return R.success();
    }
}
