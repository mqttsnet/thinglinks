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
             * @param job
             * @return AjaxResult
             * @throws TaskException
             * @Description: 新增任务
             */
            @Override
            public AjaxResult add(SysJob job) throws SchedulerException, TaskException {
                return AjaxResult.error("新增任务失败:" + throwable.getMessage());
            }
        };
    }
}
