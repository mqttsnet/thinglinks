package com.mqttsnet.thinglinks.config.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 * 自定义线程池配置工具类
 *
 * @author Sun ShiHuan
 * @version 1.0.4
 * @since 2025/6/17
 */
public final class ThreadPoolConfig {
    public static ThreadPoolExecutor newFixedExecutor(String namePrefix,
                                                      int coreSize,
                                                      int maxSize,
                                                      int queueCapacity) {
        return new ThreadPoolExecutor(
            coreSize,
            maxSize,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueCapacity),
            new NamedThreadFactory(namePrefix),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger();
        private final String namePrefix;

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix + "-" + counter.incrementAndGet());
        }
    }
}
