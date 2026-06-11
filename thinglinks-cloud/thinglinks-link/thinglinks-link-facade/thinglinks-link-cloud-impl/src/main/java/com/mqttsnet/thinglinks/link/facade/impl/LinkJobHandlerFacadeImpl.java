package com.mqttsnet.thinglinks.link.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.link.api.job.LinkJobHandlerApi;
import com.mqttsnet.thinglinks.link.facade.LinkJobHandlerFacade;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradeTasksResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tangyh
 * @since 2024/12/24 17:02
 */
@Service
public class LinkJobHandlerFacadeImpl implements LinkJobHandlerFacade {
    @Lazy
    @Autowired
    private LinkJobHandlerApi linkJobHandlerApi;

    @Override
    public R<?> refreshDeviceCacheForTenant(Long tenantId) {
        return linkJobHandlerApi.refreshDeviceCacheForTenant(tenantId);
    }

    @Override
    public R<?> syncDeviceConnectionStatus(Long tenantId) {
        return linkJobHandlerApi.syncDeviceConnectionStatus(tenantId);
    }

    @Override
    public R<?> refreshProductCacheForTenant(Long tenantId) {
        return linkJobHandlerApi.refreshProductCacheForTenant(tenantId);
    }

    @Override
    public R<?> refreshProductModelCache(Long tenantId) {
        return linkJobHandlerApi.refreshProductModelCache(tenantId);
    }

    @Override
    public R<?> otaUpgradeTasksExecute(Long tenantId) {
        return linkJobHandlerApi.otaUpgradeTasksExecute(tenantId);
    }
}
