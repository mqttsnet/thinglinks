package com.mqttsnet.thinglinks.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mqttsnet.basic.utils.topic.MqttTopicMatcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.SampleTime})
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
// 模拟64线程并发
@Threads(64)
// 3次独立测试
@Fork(3)
public class AclMatcherOptimizedBenchmarkTest {

    // 匹配统计
    private static final AtomicLong totalRequests = new AtomicLong();
    private static final AtomicLong matchedCount = new AtomicLong();
    private static final AtomicLong cacheHits = new AtomicLong();
    // 延迟统计桶（优化版）
    private static final long[] latencyBuckets = new long[8];
    private static final long[] latencyThresholds = {
        1000,    // 1ms
        5000,    // 5ms
        10000,   // 10ms
        50000,   // 50ms
        100000,  // 100ms
        500000,  // 500ms
        1000000, // 1000ms
        Long.MAX_VALUE
    };
    // 线程池管理
    private static ExecutorService executor;
    // 数据结构优化
    private List<PatternTopicPair> patternTopicPairs;
    private Random random;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(AclMatcherOptimizedBenchmarkTest.class.getSimpleName())
            .jvmArgs("-Xmx16g", "-Xms16g", "-XX:+UseZGC", "-XX:MaxGCPauseMillis=10")
            .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void initTestData() {
        random = new Random();
        // 核心线程数=CPU核心数，最大线程数=2*CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maxPoolSize = corePoolSize * 2;
        executor = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            new ThreadFactoryBuilder()
                .setNameFormat("acl-matcher-pool-%d")
                .setDaemon(true)
                .build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        patternTopicPairs = generatePatternTopicPairs(10_000);
        System.out.println("Test data initialized: " + patternTopicPairs.size() + " pattern-topic pairs");
    }

    @TearDown(Level.Trial)
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        printStatistics();
    }

    @Benchmark
    public void testPatternMatching(Blackhole bh) {
        PatternTopicPair pair = patternTopicPairs.get(random.nextInt(patternTopicPairs.size()));

        long start = System.nanoTime();
        boolean result = MqttTopicMatcher.match(pair.pattern, pair.topic);
        long duration = System.nanoTime() - start;

        // 统计
        totalRequests.incrementAndGet();
        if (result) {
            matchedCount.incrementAndGet();
        }

        // 用耗时下限近似估算"快路径"占比(算法已下沉到 util-pro MqttTopicMatcher,无 Pattern 编译缓存)
        if (duration < 100_000) { // <0.1ms 视为快路径
            cacheHits.incrementAndGet();
        }

        recordLatency(duration);
        bh.consume(result);
    }

    // 生成匹配的pattern-topic对
    private List<PatternTopicPair> generatePatternTopicPairs(int count) {
        List<PatternTopicPair> pairs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String pattern = generatePattern();
            String topic = generateMatchingTopic(pattern);
            pairs.add(new PatternTopicPair(pattern, topic));
        }
        return pairs;
    }

    private String generatePattern() {
        int type = random.nextInt(100);
        if (type < 5) {
            return "$SYS/monitor/" + random.nextInt(20) + "/stats";
        }
        if (type < 20) {
            return "city/" + random.nextInt(50) + "/#";
        }
        if (type < 50) {
            return "building/" + random.nextInt(100) + "/+/status";
        }
        return "device/" + random.nextInt(10000) + "/sensor/" + random.nextInt(100);
    }

    private String generateMatchingTopic(String pattern) {
        if (pattern.contains("#")) {
            return pattern.replace("#", "level1/level2/level3");
        }
        if (pattern.contains("+")) {
            return pattern.replace("+", "sensor_" + random.nextInt(1000));
        }
        return pattern; // 精确匹配
    }

    private void recordLatency(long nanos) {
        long micros = nanos / 1000;
        for (int i = 0; i < latencyThresholds.length; i++) {
            if (micros < latencyThresholds[i]) {
                latencyBuckets[i]++;
                break;
            }
        }
    }

    private void printStatistics() {
        long total = totalRequests.get();
        long matched = matchedCount.get();
        long hits = cacheHits.get();

        System.out.println("\n=============== 压测结果 ===============");
        System.out.printf("总请求量: %,d\n", total);
        System.out.printf("匹配成功率: %.2f%%\n", (matched * 100.0) / total);
        System.out.printf("缓存命中率: %.2f%%\n", (hits * 100.0) / total);

        System.out.println("\n延迟分布 (μs):");
        String[] labels = {"<1ms", "<5ms", "<10ms", "<50ms", "<100ms", "<500ms", "<1000ms", ">1000ms"};
        for (int i = 0; i < labels.length; i++) {
            System.out.printf("  %-8s: %,d (%.2f%%)\n",
                labels[i],
                latencyBuckets[i],
                (latencyBuckets[i] * 100.0) / total);
        }

        // v1.1 起算法已下沉到 util-pro MqttTopicMatcher,无 Pattern 编译缓存 ──
        // 改为分段字符串比较,Pattern 编译不再发生,原 caffeine 缓存指标已移除。
        System.out.println("=========================================");
    }

    // 数据结构优化
    private static class PatternTopicPair {
        final String pattern;
        final String topic;

        PatternTopicPair(String pattern, String topic) {
            this.pattern = pattern;
            this.topic = topic;
        }
    }
}
