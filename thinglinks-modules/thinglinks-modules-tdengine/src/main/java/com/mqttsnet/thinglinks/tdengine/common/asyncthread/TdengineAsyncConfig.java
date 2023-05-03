package com.mqttsnet.thinglinks.tdengine.common.asyncthread;

import com.mqttsnet.thinglinks.common.core.asyncthread.SuRejectHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步@Async多线程配置
 *
 * @author thinglinks
 */
@Configuration
@EnableAsync
public class TdengineAsyncConfig {

    /**
     * 核心线程数（默认线程数）
     */
    @Value("${threadBus.pool.core-pool-size}")
    private int corePoolSize = Runtime.getRuntime().availableProcessors();
    /**
     * 最大线程数
     */
    @Value("${threadBus.pool.max-pool-size}")
    private int maxPoolSize = Runtime.getRuntime().availableProcessors();
    /**
     * 允许线程空闲时间（单位：默认为秒）
     */
    @Value("${threadBus.pool.keep-alive-time}")
    private int keepAliveTime = 60;
    /**
     * 缓冲队列大小
     */
    @Value("${threadBus.pool.queue-capacity}")
    private int queueCapacity = 200;
    /**
     * 线程池名前缀
     */
    @Value("${threadBus.pool.thread-name-prefix}")
    private String threadNamePrefix = "thinglinksAsync-";

    @Autowired
    private SuRejectHandle rejectHandle;


    @Bean("tdengineAsync")
    public Executor tdengineAsync() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);//核心线程数
        executor.setMaxPoolSize(maxPoolSize);//最大线程数  cpu核数/(1-0.8)//cup核数*2//cup核数+1
        //传参正值使用无界LinkedBlockingQueue
        //其他传参使用不缓存SynchronousQueue
        executor.setQueueCapacity(queueCapacity);//队列长度(超过队列长度无法存储,则开启最大线程数)
        executor.setKeepAliveSeconds(keepAliveTime);//空闲线程最大存活时间 默认60s
        executor.setThreadNamePrefix(threadNamePrefix+"tdengineAsync-");//线程名前缀
        executor.setRejectedExecutionHandler(rejectHandle);// 自定义任务丢失处理策略   该策略输出由scheduling-1打印
        //设置线程池等待所有任务都完成再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(keepAliveTime);
        executor.initialize();
        return executor;
    }
}
