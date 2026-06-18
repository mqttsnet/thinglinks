package com.mqttsnet.thinglinks.iot.executor.service.jobhandler;

import java.util.function.Consumer;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.iot.executor.service.AbstractTenantJob;
import com.mqttsnet.thinglinks.link.facade.LinkJobHandlerFacade;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Job handler for link tasks.
 *
 * @author mqttsnet
 */
@Component
@Slf4j
public class LinkJob extends AbstractTenantJob {

    @Autowired
    @Lazy
    private LinkJobHandlerFacade linkJobHandlerApi;


    /**
     * 全租户设备缓存刷新调度任务
     */
    @XxlJob("flushAnyTenantDeviceCacheJobHandler")
    public void flushAnyTenantDeviceCacheJobHandler() {
        XxlJobHelper.log("[刷新设备缓存]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();

            // ✅ 设置上下文
            ContextUtil.setTenantId(tenantId);

            // ✅ 触发执行缓存刷新
            executeDeviceCacheRefresh(tenantId);
        });
    }

    /**
     * 执行设备缓存刷新
     *
     * @param tenantId 租户ID
     */
    public void executeDeviceCacheRefresh(Long tenantId) {
        long tenantStart = System.currentTimeMillis();

        try {
            log.info("租户缓存刷新开始 tenantId={}", tenantId);
            XxlJobHelper.log("租户缓存刷新开始 tenantId={}", tenantId);
            // 设备缓存
            refreshCache(tenantId, "设备缓存", linkJobHandlerApi::refreshDeviceCacheForTenant);

            long totalCost = System.currentTimeMillis() - tenantStart;
            log.info("租户缓存刷新完成 tenantId={} | 耗时={}ms", tenantId, totalCost);
            XxlJobHelper.log("租户缓存刷新完成 tenantId={} | total={}ms", tenantId, totalCost);
        } catch (Exception e) {
            long failedCost = System.currentTimeMillis() - tenantStart;
            log.error("租户缓存刷新失败 tenantId={} | cost={}ms | error={}", tenantId, failedCost, e.getMessage(), e);
            XxlJobHelper.log("租户缓存刷新失败 tenantId={} | error={}", tenantId, e.getMessage());
        }
    }


    /**
     * 全租户产品缓存刷新调度任务
     */
    @XxlJob("flushAnyTenantProductCacheJobHandler")
    public void flushAnyTenantProductCacheJobHandler() {
        XxlJobHelper.log("[刷新产品缓存]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();

            // ✅ 设置上下文
            ContextUtil.setTenantId(tenantId);

            // ✅ 触发执行缓存刷新
            executeProductCacheRefresh(tenantId);
        });
    }


    /**
     * 全租户产品缓存刷新调度任务
     *
     * @param tenantId
     */
    public void executeProductCacheRefresh(Long tenantId) {
        long tenantStart = System.currentTimeMillis();

        try {
            log.info("租户缓存刷新开始 tenantId={}", tenantId);
            XxlJobHelper.log("租户缓存刷新开始 tenantId={}", tenantId);
            // 产品缓存
            refreshCache(tenantId, "产品缓存", linkJobHandlerApi::refreshProductCacheForTenant);

            long totalCost = System.currentTimeMillis() - tenantStart;
            log.info("租户缓存刷新完成 tenantId={} | 耗时={}ms", tenantId, totalCost);
            XxlJobHelper.log("租户缓存刷新完成 tenantId={} | total={}ms", tenantId, totalCost);
        } catch (Exception e) {
            long failedCost = System.currentTimeMillis() - tenantStart;
            log.error("租户缓存刷新失败 tenantId={} | cost={}ms | error={}", tenantId, failedCost, e.getMessage(), e);
            XxlJobHelper.log("租户缓存刷新失败 tenantId={} | error={}", tenantId, e.getMessage());
        }
    }

    /**
     * 全租户产品模型缓存刷新调度任务
     */
    @XxlJob("flushAnyTenantProductModelCacheJobHandler")
    public void flushAnyTenantProductModelCacheJobHandler() {
        XxlJobHelper.log("[刷新产品模型缓存]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();

            // ✅ 设置上下文
            ContextUtil.setTenantId(tenantId);

            // ✅ 触发执行缓存刷新
            executeProductModelCacheRefresh(tenantId);
        });
    }

    /**
     * 全租户产品版本发布重试兜底调度任务 ── 独立于缓存预热,与之解耦:
     * 故障互不影响、周期可单独加密(建议 2~5 分钟,确保 1h 兜底窗口内多次扫描)。
     * 单租户异常被捕获不外抛,不中断其余租户。
     */
    @XxlJob("flushProductVersionPublishRetryJobHandler")
    public void flushProductVersionPublishRetryJobHandler() {
        XxlJobHelper.log("[产品发布重试兜底]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            ContextUtil.setTenantId(tenantId);
            long start = System.currentTimeMillis();
            try {
                linkJobHandlerApi.retryProductVersionPublish(tenantId);
                XxlJobHelper.log("发布重试兜底完成 tenantId={} | cost={}ms", tenantId, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("发布重试兜底异常 tenantId={} | error={}", tenantId, e.getMessage(), e);
                XxlJobHelper.log("发布重试兜底异常 tenantId={} | error={}", tenantId, e.getMessage());
            }
        });
    }


    public void executeProductModelCacheRefresh(Long tenantId) {
        long tenantStart = System.currentTimeMillis();

        try {
            log.info("租户缓存刷新开始 tenantId={}", tenantId);
            XxlJobHelper.log("租户缓存刷新开始 tenantId={}", tenantId);
            // 产品模型
            refreshCache(tenantId, "产品模型缓存", linkJobHandlerApi::refreshProductModelCache);

            long totalCost = System.currentTimeMillis() - tenantStart;
            log.info("租户缓存刷新完成 tenantId={} | 耗时={}ms", tenantId, totalCost);
            XxlJobHelper.log("租户缓存刷新完成 tenantId={} | total={}ms", tenantId, totalCost);
        } catch (Exception e) {
            long failedCost = System.currentTimeMillis() - tenantStart;
            log.error("租户缓存刷新失败 tenantId={} | cost={}ms | error={}", tenantId, failedCost, e.getMessage(), e);
            XxlJobHelper.log("租户缓存刷新失败 tenantId={} | error={}", tenantId, e.getMessage());
        }
    }


    /**
     * Refreshes the cache for a specific tenant and logs the duration.
     *
     * @param tenantId Identifier of the tenant.
     * @param type     Type of cache to refresh (e.g., device, product).
     * @param action   The cache refreshing action.
     */
    protected void refreshCache(Long tenantId, String type, Consumer<Long> action) {
        long start = System.currentTimeMillis();
        try {
            log.info("缓存刷新开始 tenantId={} | type={}", tenantId, type);
            action.accept(tenantId);
            long cost = System.currentTimeMillis() - start;
            log.info("缓存刷新完成 tenantId={} | type={} | cost={}ms", tenantId, type, cost);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("缓存刷新异常 tenantId={} | type={} | cost={}ms | error={}", tenantId, type, cost, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Initiates the synchronization of device connection status for all tenants.
     * This method loads each tenant, sets the appropriate tenant context, and then triggers the device connection
     * status sync operation for the tenant.
     *
     * <p><strong>Usage:</strong> This method is invoked as part of a scheduled task to keep the device connection
     * statuses up-to-date across all tenants. It logs the duration for each tenant's sync operation.</p>
     *
     * <p><strong>Scheduling:</strong> This job is scheduled to run at regular intervals (e.g., hourly). The frequency
     * of execution can be adjusted through the XXL-Job console.</p>
     *
     * @XxlJob The annotation indicating that this method is a scheduled job under the XXL-Job framework.
     * @see LinkJobHandlerFacade#syncDeviceConnectionStatus(Long) for device connection status sync logic.
     */
    @XxlJob("syncAnyTenantDeviceConnectionStatusJobHandler")
    public void syncAnyTenantDeviceConnectionStatusJobHandler() {
        XxlJobHelper.log("[设备连接状态同步]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            try {
                // ✅ 设置上下文
                ContextUtil.setTenantId(tenantId);

                log.info("[设备连接状态同步]租户任务开始 tenantId={}", tenantId);
                XxlJobHelper.log("[设备连接状态同步]租户任务开始 tenantId={}", tenantId);

                deviceConnectionStatus(tenantId);
            } catch (Exception e) {
                log.error("[设备连接状态同步]租户任务异常 tenantId={} | error={}", tenantId, e.getMessage(), e);
                XxlJobHelper.log("[设备连接状态同步]租户任务异常 tenantId={} | error={}", tenantId, e.getMessage());
            }
        });
    }

    /**
     * Synchronizes the device connection status for a specific tenant.
     *
     * @param tenantId Identifier of the tenant.
     */
    protected void deviceConnectionStatus(Long tenantId) {
        final long start = System.currentTimeMillis();
        try {
            R<?> response = linkJobHandlerApi.syncDeviceConnectionStatus(tenantId);
            long cost = System.currentTimeMillis() - start;
            if (response.getIsSuccess()) {
                log.info("[设备连接状态同步]同步成功 tenantId={} | 耗时={}ms | 结果={}", tenantId, cost, response.getData());
                XxlJobHelper.log("[设备连接状态同步]同步成功 tenantId={} | 耗时={}ms", tenantId, cost);
            } else {
                log.warn("[设备连接状态同步]同步失败 tenantId={} | 耗时={}ms | 错误码={} | 错误信息={}", tenantId, cost, response.getCode(), response.getMsg());
                XxlJobHelper.log("[设备连接状态同步]同步失败 tenantId={} | 错误码={}", tenantId, response.getCode());
            }

        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[设备连接状态同步]同步异常 tenantId={} | 耗时={}ms | 错误={}", tenantId, cost, e.getMessage(), e);
            XxlJobHelper.log("[设备连接状态同步]同步异常 tenantId={} | 错误={}", tenantId, e.getMessage());
        }
    }


    /**
     * Executes OTA upgrade tasks for either a specific tenant provided via job parameters
     * or all tenants. A tenant ID can be manually specified in the job's JSON parameters to
     * target a specific tenant; otherwise, the method iterates over all tenants.
     * <p>
     * JSON parameter format: {"TenantId": ""}
     * Default: No tenant ID provided, so all tenants are processed.
     * </p>
     */
    @XxlJob("processOtaUpgradeTasksJobHandler")
    public void processOtaUpgradeTasksJobHandler() {
        XxlJobHelper.log("[OTA升级]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            try {
                // ✅ 设置上下文
                ContextUtil.setTenantId(tenantId);
                log.info("[OTA升级]租户任务开始 tenantId={}", tenantId);
                XxlJobHelper.log("[OTA升级]租户任务开始 tenantId={}", tenantId);

                processOtaUpgradeForTenant(tenantId);
            } catch (Exception e) {
                log.error("[OTA升级]租户任务异常 tenantId={} | error={}", tenantId, e.getMessage(), e);
                XxlJobHelper.log("[OTA升级]租户任务异常 tenantId={} | error={}", tenantId, e.getMessage());
            }
        });
    }

    /**
     * 处理OTA升级任务
     *
     * @param tenantId 租户ID
     */
    protected void processOtaUpgradeForTenant(Long tenantId) {
        final long start = System.currentTimeMillis();
        try {
            R<?> result = linkJobHandlerApi.otaUpgradeTasksExecute(tenantId);
            long cost = System.currentTimeMillis() - start;
            if (result.getIsSuccess()) {
                log.info("[OTA升级]任务成功 tenantId={} | 耗时={}ms", tenantId, cost);
                XxlJobHelper.log("[OTA升级]任务成功 tenantId={} | 耗时={}ms", tenantId, cost);
            } else {
                log.warn("[OTA升级]业务失败 tenantId={} | 耗时={}ms | 错误码={} | 错误信息={}", tenantId, cost, result.getCode(), result.getMsg());
                XxlJobHelper.log("[OTA升级]业务失败 tenantId={} | 错误码={}", tenantId, result.getCode());
            }
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[OTA升级]系统异常 tenantId={} | 耗时={}ms | 异常类型={} | 错误信息={}", tenantId, cost, e.getClass().getSimpleName(), e.getMessage(), e);
            XxlJobHelper.log("[OTA升级]系统异常 tenantId={} | 错误={}", tenantId, e.getMessage());
        }
    }
}
