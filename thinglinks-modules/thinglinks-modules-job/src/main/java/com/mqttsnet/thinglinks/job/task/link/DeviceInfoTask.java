package com.mqttsnet.thinglinks.job.task.link;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: thinglinks
 * @description: 子设备管理管理定时任务
 * @packagename: com.mqttsnet.thinglinks.job.task.link
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-11 15:08
 **/
@Component("linkDeviceInfoTask")
@Slf4j
public class DeviceInfoTask {

    @Resource
    private RemoteDeviceInfoService remoteDeviceInfoService;


    /**
     * 刷新子设备数据模型定时任务
     */
    public void refreshDeviceInfoDataModel(String params) {
        StopWatch watch = new StopWatch();
        watch.start();
        if (log.isInfoEnabled()) {
            log.info("刷新子设备数据模型定时任务开始，参数：{}", params);
        }
        List<Long> collect = StringUtils.isNotEmpty(params)?Arrays.stream(params.split(",")).map(Long::valueOf).collect(Collectors.toList()):new ArrayList<>();
        remoteDeviceInfoService.refreshDeviceInfoDataModel(collect.toArray(new Long[0]));
        watch.stop();
        if (log.isInfoEnabled()) {
            log.info("刷新子设备数据模型定时任务结束，耗时(millisecond)：{}", watch.getTime());
        }
    }
}
