package com.mqttsnet.thinglinks.job.api.factory;

import com.mqttsnet.thinglinks.common.core.exception.job.TaskException;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.job.api.RemoteJobService;
import com.mqttsnet.thinglinks.job.api.domain.SysJob;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @ClassDescription: Job服务降级处理
 * @ClassName: RemoteJobFallbackFactory
 * @Author: thinglinks
 * @Date: 2021-12-31 11:00:59
 * @Version 1.0
 */
@Component
public class RemoteJobFallbackFactory implements FallbackFactory<RemoteJobService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteJobFallbackFactory.class);

    @Override
    public RemoteJobService create(Throwable throwable) {
        log.error("Job服务调用失败:{}", throwable.getMessage());
        return new RemoteJobService()
        {


            /**
             * 新增任务
             * @param job
             * @return AjaxResult
             * @throws TaskException
             * @Description: 新增任务
             */
            @Override
            public AjaxResult add(SysJob job) throws SchedulerException, TaskException {
                return AjaxResult.error("新增任务失败:" + throwable.getMessage());
            }

            /**
             * 获取定时任务详细信息
             *
             * @param jobId
             */
            @Override
            public AjaxResult getInfo(Long jobId) {
                return AjaxResult.error("获取定时任务详细信息失败:" + throwable.getMessage());
            }

            /**
             * 修改定时任务
             *
             * @param job
             */
            @Override
            public AjaxResult edit(SysJob job) throws SchedulerException, TaskException {
                return AjaxResult.error("修改定时任务失败:" + throwable.getMessage());
            }

            /**
             * 定时任务状态修改
             *
             * @param job
             */
            @Override
            public AjaxResult changeStatus(SysJob job) throws SchedulerException {
                return AjaxResult.error("定时任务状态修改失败:" + throwable.getMessage());
            }

            /**
             * 定时任务立即执行一次
             *
             * @param job
             */
            @Override
            public AjaxResult run(SysJob job) throws SchedulerException {
                return AjaxResult.error("定时任务立即执行一次失败:" + throwable.getMessage());
            }

            /**
             * 删除定时任务
             *
             * @param jobIds
             */
            @Override
            public AjaxResult remove(Long[] jobIds) throws SchedulerException, TaskException {
                return AjaxResult.error("删除定时任务失败:" + throwable.getMessage());
            }
        };
    }
}
