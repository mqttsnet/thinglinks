package com.mqttsnet.thinglinks.device.service;

/**
 * -----------------------------------------------------------------------------
 * File Name: DeviceSyncInnerService
 * -----------------------------------------------------------------------------
 * Description:
 * 设备数据同步业务层接口
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2025/1/11       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2025/1/11 17:23
 */
public interface DeviceSyncInnerService {


    /**
     * 同步设备连接状态
     *
     * @param tenantId 租户ID
     */
    void syncDeviceConnectionStatus(Long tenantId);
}
