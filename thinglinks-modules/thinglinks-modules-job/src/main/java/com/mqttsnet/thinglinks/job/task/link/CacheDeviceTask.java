package com.mqttsnet.thinglinks.job.task.link;

import com.mqttsnet.thinglinks.link.api.RemoteCacheOpenAnyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * -----------------------------------------------------------------------------
 * File Name: CacheDeviceTask
 * -----------------------------------------------------------------------------
 * Description:
 * 刷新设备缓存定时任务
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/3/24       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2024/3/24 15:14
 */
@Component("cacheDeviceTask")
@Slf4j
public class CacheDeviceTask {

    @Resource
    private RemoteCacheOpenAnyService remoteCacheOpenAnyService;

    /**
     * Refreshes the cache for all devices within a tenant.
     * This is a scheduled task.
     */
    public void refreshAllDeviceCaches(String params) {
        StopWatch watch = new StopWatch();
        watch.start();
        log.info("Starting to refresh all device caches.");

        remoteCacheOpenAnyService.refreshAllDeviceCaches();

        watch.stop();
        log.info("Finished refreshing all device caches. Duration (milliseconds): {}", watch.getTime());
    }

}
