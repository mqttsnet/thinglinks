package com.mqttsnet.thinglinks.job.task.link;

import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.RemoteProtocolService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: thinglinks
 * @description: 协议管理定时任务
 * @packagename: com.mqttsnet.thinglinks.job.task.link
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-11 15:08
 **/
@Component("linkProtocolTask")
@Slf4j
public class ProtocolTask {

    @Resource
    private RemoteProtocolService remoteProtocolService;


    /**
     * 协议脚本缓存刷新定时任务
     */
    public void protocolScriptCacheRefreshTask() {
        StopWatch watch = new StopWatch();
        watch.start();
        if (log.isInfoEnabled()) {
            log.info("协议脚本缓存刷新定时任务开始");
        }
        AjaxResult ajaxResult = remoteProtocolService.protocolScriptCacheRefresh();
        watch.stop();
        if (log.isInfoEnabled()) {
            log.info("协议脚本缓存刷新定时任务结束，耗时(millisecond)：{}", watch.getTime());
        }
    }
}
