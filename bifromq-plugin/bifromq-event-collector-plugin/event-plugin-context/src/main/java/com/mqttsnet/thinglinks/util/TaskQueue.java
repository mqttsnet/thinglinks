package com.mqttsnet.thinglinks.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskQueue {

    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor executorService;

    /**
     * Constructor to initialize the TaskQueue with the given parameters.
     *
     * @param corePoolSize    The number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize The maximum number of threads to allow in the pool.
     * @param keepAliveTime   When the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit            The time unit for the keepAliveTime argument.
     */
    public TaskQueue(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        log.info("Initializing TaskQueue...");
        this.executorService = new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            unit,
            new LinkedBlockingQueue<>(),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "TaskQueueThread-" + counter.incrementAndGet());
                }
            });

        for (int i = 0; i < corePoolSize; i++) {
            executorService.submit(() -> {
                try {
                    while (true) {
                        Runnable task = tasks.take(); // This will block if the queue is empty
                        task.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Task execution was interrupted on thread: " + Thread.currentThread().getName());
                }
            });
        }
    }

    /**
     * Add a new task to the task queue.
     *
     * @param task The task to be added to the queue.
     */
    public void addTask(Runnable task) {
        try {
            tasks.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to add task, executed on thread: " + Thread.currentThread().getName(), e);
        }
    }

    /**
     * Safely shuts down the task queue.
     */
    public void shutdown() {
        log.info("Shutting down TaskQueue...");
        executorService.shutdownNow(); // This will interrupt any blocking tasks
    }
}
