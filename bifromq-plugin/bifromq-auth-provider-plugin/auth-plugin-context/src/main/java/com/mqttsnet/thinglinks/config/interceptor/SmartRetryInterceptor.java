package com.mqttsnet.thinglinks.config.interceptor;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Description:
 * SmartRetryInterceptor is an interceptor for OkHttp that implements a smart retry mechanism.
 *
 * @author mqttsnet
 * @version 1.0.4
 * @since 2025/6/9
 */
public class SmartRetryInterceptor implements Interceptor {
    private final int maxRetries;
    private final long baseDelayMs;

    public SmartRetryInterceptor(int maxRetries, long baseDelayMs) {
        this.maxRetries = maxRetries;
        this.baseDelayMs = baseDelayMs;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        int attempt = 0;
        Response response = null;

        while (true) {
            try {
                response = chain.proceed(request);

                // 检查是否需要重试
                if (!shouldRetry(response, attempt)) {
                    return response;
                }

                closeResponse(response);
                waitBeforeRetry(attempt);
                attempt++;

            } catch (IOException e) {
                // 检查是否可重试的异常
                if (!shouldRetry(e, attempt)) {
                    throw e;
                }
                waitBeforeRetry(attempt);
                attempt++;
            }
        }
    }

    private boolean shouldRetry(Response response, int attempt) {
        if (attempt >= maxRetries) return false;

        int code = response.code();
        String method = response.request().method();

        // 对GET请求的服务器错误重试
        if ("GET".equalsIgnoreCase(method) && (code >= 500 || code == 429)) {
            return true;
        }

        // 对特定状态码的非GET请求重试
        return code == 429; // 仅对请求过多重试非GET请求
    }

    private boolean shouldRetry(IOException e, int attempt) {
        if (attempt >= maxRetries) return false;

        // 可重试的异常类型
        return e instanceof SocketTimeoutException ||
            e instanceof ConnectException ||
            e instanceof SSLException;
    }

    private void waitBeforeRetry(int attempt) {
        try {
            long delay = baseDelayMs * (long) Math.pow(2, attempt);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeResponse(Response response) {
        if (response != null && response.body() != null) {
            try {
                response.body().close();
            } catch (Exception ignored) {
            }
        }
    }
}
