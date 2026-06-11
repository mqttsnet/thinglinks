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

        // ─ 步骤 1:retry 兜底 ──────────────────────────────────────────
        // 扫描 status=RUNNING 的 publish_record:
        // - 事件路径成功的记录已经是 SUCCESS,这里跳过(过滤条件)
        // - 事件路径异常 / JVM 重启 / 网络抖动导致卡 RUNNING 的,在这里重试
        // - 超过 1h 仍 RUNNING 的视为永久失败,标 FAILED 不再重试
        // 这一步在缓存刷新之前做,因为重试可能改 active 版本对应的 stable / 设备绑定,
        // 重试完再刷缓存才能拿到最新状态。
        int retried = productVersionPublishOrchestrator.retryRunningRecordsForTenant(tenantId);
        if (retried > 0) {
            log.info("[link-job] retried {} RUNNING publish records for tenant {}", retried, tenantId);
        }

        // ─ 步骤 2:预热物模型缓存 ─────────────────────────────────────
        // 按版本快照维度遍历刷新:每个产品的每个激活态版本 (PUBLISHED/CANARY/SHADOW) 都被预热,
        // 避免灰度场景设备命中老版本时还要走 read-through DB IO。
        log.info("Refreshing all product model cache for tenant (all products × all active versions): {}", tenantId);
        productModelCacheService.refreshAllProductModelCacheForTenant(tenantId);
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
