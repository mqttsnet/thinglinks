package com.mqttsnet.thinglinks.job.api;

/**
 * @InterfaceDescription: Job服务
 * @InterfaceName: RemoteJobService
 * @Author: thinglinks
 * @Date: 2021-12-31 10:57:16
 * @Version 1.0
 */

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.exception.job.TaskException;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.job.api.domain.SysJob;
import com.mqttsnet.thinglinks.job.api.factory.RemoteJobFallbackFactory;
import org.quartz.SchedulerException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(contextId = "remoteJobService", value = ServiceNameConstants.THINGLINKS_JOB, fallbackFactory = RemoteJobFallbackFactory.class)
public interface RemoteJobService {
    /**
     * @Description: 新增任务
     * @param job
     * @return AjaxResult
     * @throws TaskException
     */
    @PostMapping
    public AjaxResult add(@RequestBody SysJob job) throws SchedulerException, TaskException;

    /**
     * 获取定时任务详细信息
     */
    @GetMapping(value = "/{jobId}")
    public AjaxResult getInfo(@PathVariable("jobId") Long jobId);

    /**
     * 修改定时任务
     */
    @PutMapping
    public AjaxResult edit(@RequestBody SysJob job) throws SchedulerException, TaskException;

    /**
     * 定时任务状态修改
     */
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysJob job) throws SchedulerException;

    /**
     * 定时任务立即执行一次
     */
    @PutMapping("/run")
    public AjaxResult run(@RequestBody SysJob job) throws SchedulerException;

    /**
     * 删除定时任务
     */
    @DeleteMapping("/{jobIds}")
    public AjaxResult remove(@PathVariable Long[] jobIds) throws SchedulerException, TaskException;
}
