package com.mqttsnet.thinglinks.video.media.common;

import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Description:
 * 带重试能力的流媒体 REST 客户端装饰器。
 *
 * <p>包装任意 {@link MediaRestClient} 实现，在请求失败时按配置策略进行重试。
 * 支持指数退避策略，确保流媒体服务器临时不可用时自动恢复。</p>
 *
 * <p>设计要点：</p>
 * <ul>
 *     <li>装饰器模式：不修改原有 RestClient 实现，透明增加重试能力</li>
 *     <li>全链路日志：每次重试均记录详细日志，确保留痕</li>
 *     <li>可配置参数：最大重试次数、初始重试间隔、退避倍率</li>
 *     <li>只对网络异常（超时/连接失败）重试，业务错误不重试</li>
 * </ul>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
public class RetryableMediaRestClient implements MediaRestClient {

    private final MediaRestClient delegate;
    private final int maxRetries;
    private final long initialIntervalMs;
    private final double multiplier;

    /**
     * @param delegate          被装饰的原始 RestClient
     * @param maxRetries        最大重试次数（不包括首次请求）
     * @param initialIntervalMs 首次重试间隔（毫秒）
     * @param multiplier        退避倍率（如 2.0 表示每次间隔翻倍）
     */
    public RetryableMediaRestClient(MediaRestClient delegate, int maxRetries, long initialIntervalMs, double multiplier) {
        this.delegate = delegate;
        this.maxRetries = maxRetries;
        this.initialIntervalMs = initialIntervalMs;
        this.multiplier = multiplier;
    }

    /**
     * 使用默认重试策略（3次重试、1秒初始间隔、2倍退避）
     *
     * @param delegate 被装饰的原始 RestClient
     */
    public RetryableMediaRestClient(MediaRestClient delegate) {
        this(delegate, 3, 1000L, 2.0);
    }

    @Override
    public MediaApiResult get(VideoMediaServer mediaServer, String api, Map<String, Object> params) {
        return executeWithRetry("GET", mediaServer, api, () -> delegate.get(mediaServer, api, params));
    }

    @Override
    public MediaApiResult post(VideoMediaServer mediaServer, String api, JSONObject body) {
        return executeWithRetry("POST", mediaServer, api, () -> delegate.post(mediaServer, api, body));
    }

    @Override
    public MediaApiResult postForm(VideoMediaServer mediaServer, String api, Map<String, Object> params) {
        return executeWithRetry("POST_FORM", mediaServer, api, () -> delegate.postForm(mediaServer, api, params));
    }

    @Override
    public String buildUrl(VideoMediaServer mediaServer, String api) {
        return delegate.buildUrl(mediaServer, api);
    }

    /**
     * 带重试的执行逻辑
     */
    private MediaApiResult executeWithRetry(String method, VideoMediaServer mediaServer, String api,
                                            MediaApiResultSupplier supplier) {
        int attempt = 0;
        long intervalMs = initialIntervalMs;
        MediaApiResult lastResult = null;

        while (attempt <= maxRetries) {
            if (attempt > 0) {
                log.warn("[媒体API重试] 第{}次重试 method={}, server={}:{}, api={}, 间隔={}ms",
                        attempt, method, mediaServer.getHost(), mediaServer.getHttpPort(), api, intervalMs);
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[媒体API重试] 重试被中断 method={}, api={}", method, api);
                    return lastResult != null ? lastResult : MediaApiResult.fail(-3, "重试被中断");
                }
                intervalMs = Math.min((long) (intervalMs * multiplier), 60_000L);
            }

            lastResult = supplier.get();

            // 业务成功或非网络错误时不重试
            if (lastResult.isSuccess() || lastResult.getCode() != -1) {
                if (attempt > 0 && lastResult.isSuccess()) {
                    log.info("[媒体API重试] 重试成功 method={}, server={}:{}, api={}, 共重试{}次",
                            method, mediaServer.getHost(), mediaServer.getHttpPort(), api, attempt);
                }
                return lastResult;
            }

            // 超时或连接失败（code=-1），继续重试
            log.warn("[媒体API重试] 请求失败（超时/连接异常） attempt={}/{}, method={}, api={}, msg={}",
                    attempt, maxRetries, method, api, lastResult.getMsg());
            attempt++;
        }

        log.error("[媒体API重试] 重试耗尽 method={}, server={}:{}, api={}, 共尝试{}次, 最后错误={}",
                method, mediaServer.getHost(), mediaServer.getHttpPort(), api, maxRetries + 1,
                lastResult != null ? lastResult.getMsg() : "unknown");
        return lastResult != null ? lastResult : MediaApiResult.fail(-3, "重试耗尽");
    }

    /**
     * 函数式接口，延迟执行 MediaApiResult 的获取
     */
    @FunctionalInterface
    private interface MediaApiResultSupplier {
        MediaApiResult get();
    }
}
