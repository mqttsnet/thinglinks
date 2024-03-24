package com.mqttsnet.thinglinks.job.task.link;

import com.mqttsnet.thinglinks.link.api.RemoteCacheOpenAnyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * -----------------------------------------------------------------------------
 * File Name: CacheProductModelTask
 * -----------------------------------------------------------------------------
 * Description:
 * 刷新产品模型定时任务
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
 * @date 2024/3/24 15:17
 */
@Component("cacheProductModelTask")
@Slf4j
public class CacheProductModelTask {
    @Resource
    private RemoteCacheOpenAnyService remoteCacheOpenAnyService;


    /**
     * Refreshes the cache for all product models within a tenant.
     * This is a scheduled task.
     */
    public void refreshAllProductModelCaches(String params) {
        StopWatch watch = new StopWatch();
        watch.start();
        log.info("Starting to refresh all product model caches.");

        remoteCacheOpenAnyService.refreshAllProductModelCaches();

        watch.stop();
        log.info("Finished refreshing all product model caches. Duration (milliseconds): {}", watch.getTime());
    }

}
