package com.mqttsnet.thinglinks.job.task.link;

import com.mqttsnet.thinglinks.link.api.RemoteCacheOpenAnyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: thinglinks
 * @description: 子设备管理缓存刷新定时任务
 * @packagename: com.mqttsnet.thinglinks.job.task.link
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-11 15:08
 **/
@Component("cacheDeviceInfoTask")
@Slf4j
public class CacheDeviceInfoTask {

    @Resource
    private RemoteCacheOpenAnyService remoteCacheOpenAnyService;


    /**
     * Refreshes the cache for specific device information models.
     * This is a scheduled task.
     *
     * @param params Comma-separated device IDs to refresh.
     */
    public void refreshDeviceInfoDataModel(String params) {
        StopWatch watch = new StopWatch();
        watch.start();
        if (log.isInfoEnabled()) {
            log.info("刷新子设备数据模型定时任务开始，参数：{}", params);
        }
        remoteCacheOpenAnyService.refreshAllDeviceInfoCaches();
        watch.stop();
        if (log.isInfoEnabled()) {
            log.info("刷新子设备数据模型定时任务结束，耗时(millisecond)：{}", watch.getTime());
        }
    }
}
