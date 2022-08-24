package com.mqttsnet.thinglinks.common.core.asyncthread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步@Async多线程自定义拒绝策略
 *
 * @author thinglinks
 */
@Component
@Slf4j
public class SuRejectHandle implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        //打印丢失的线程任务
        log.error("线程[{}],[{}]任务丢失",Thread.currentThread().getName(),r);
    }
}
