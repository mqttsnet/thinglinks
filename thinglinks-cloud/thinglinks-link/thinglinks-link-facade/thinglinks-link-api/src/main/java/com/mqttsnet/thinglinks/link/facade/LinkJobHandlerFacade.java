package com.mqttsnet.thinglinks.link.facade;

import com.mqttsnet.basic.base.R;

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
public interface LinkJobHandlerFacade {


    /**
     * Refreshes the cache with device data for a specific tenant.
     *
     * @param tenantId Identifier of the tenant whose device cache needs to be refreshed.
     * @return Response indicating the result of the cache refresh operation.
     */
    R<?> refreshDeviceCacheForTenant(Long tenantId);


    /**
     * 同步设备连接状态
     *
     * @param tenantId 租户ID
     * @return 操作结果
     */
    R<?> syncDeviceConnectionStatus(Long tenantId);

    /**
     * Refreshes the product cache for a specific tenant.
     *
     * @param tenantId Identifier of the tenant whose product cache needs to be refreshed.
     * @return Response indicating the result of the cache refresh operation.
     */
    R<?> refreshProductCacheForTenant(Long tenantId);


    /**
     * Refreshes the product model cache for a specific tenant.
     *
     * @param tenantId Identifier of the tenant whose product model cache needs to be refreshed.
     * @return Response indicating the result of the cache refresh operation.
     */
    R<?> refreshProductModelCache(Long tenantId);


    /**
     * Executes OTA upgrade tasks for a specific tenant.
     *
     * @param tenantId Identifier of the tenant for whom OTA upgrade tasks need to be executed.
     * @return Response indicating the result of the OTA upgrade task execution.
     */
    R<?> otaUpgradeTasksExecute(Long tenantId);
}
