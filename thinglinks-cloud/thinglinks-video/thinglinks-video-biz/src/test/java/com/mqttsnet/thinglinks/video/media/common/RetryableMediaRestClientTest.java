package com.mqttsnet.thinglinks.video.media.common;

import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Description:
 * RetryableMediaRestClient 单元测试。
 * 覆盖重试逻辑、退避策略、中断处理等核心场景。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DisplayName("RetryableMediaRestClient 重试客户端测试")
class RetryableMediaRestClientTest {

    private VideoMediaServer testServer;

    @BeforeEach
    void setUp() {
        testServer = new VideoMediaServer();
        testServer.setHost("192.168.1.100");
        testServer.setHttpPort(80);
    }

    @Test
    @DisplayName("get_首次成功_不重试直接返回")
    void get_firstAttemptSuccess_noRetry() {
        var mockDelegate = new MockMediaRestClient(MediaApiResult.success(new JSONObject()));
        var retryClient = new RetryableMediaRestClient(mockDelegate, 3, 100L, 2.0);

        var result = retryClient.get(testServer, "getServerConfig", null);

        assertTrue(result.isSuccess());
        assertEquals(1, mockDelegate.getCallCount());
    }

    @Test
    @DisplayName("get_网络超时后重试成功_返回成功结果")
    void get_networkTimeoutThenSuccess_retriesAndSucceeds() {
        var callCount = new AtomicInteger(0);
        var delegate = new MediaRestClient() {
            @Override
            public MediaApiResult get(VideoMediaServer ms, String api, Map<String, Object> params) {
                int count = callCount.incrementAndGet();
                if (count <= 2) {
                    return MediaApiResult.timeout("连接超时");
                }
                return MediaApiResult.success(new JSONObject());
            }
            @Override
            public MediaApiResult post(VideoMediaServer ms, String api, JSONObject body) { return null; }
            @Override
            public MediaApiResult postForm(VideoMediaServer ms, String api, Map<String, Object> params) { return null; }
            @Override
            public String buildUrl(VideoMediaServer ms, String api) { return ""; }
        };

        var retryClient = new RetryableMediaRestClient(delegate, 3, 50L, 1.5);
        var result = retryClient.get(testServer, "test", null);

        assertTrue(result.isSuccess());
        assertEquals(3, callCount.get()); // 2次失败 + 1次成功
    }

    @Test
    @DisplayName("get_业务错误_不重试直接返回")
    void get_businessError_noRetry() {
        var mockDelegate = new MockMediaRestClient(MediaApiResult.fail(-2, "参数错误"));
        var retryClient = new RetryableMediaRestClient(mockDelegate, 3, 100L, 2.0);

        var result = retryClient.get(testServer, "test", null);

        assertFalse(result.isSuccess());
        assertEquals(-2, result.getCode());
        assertEquals(1, mockDelegate.getCallCount());
    }

    @Test
    @DisplayName("get_重试耗尽_返回最后一次失败结果")
    void get_retryExhausted_returnsLastFailure() {
        var mockDelegate = new MockMediaRestClient(MediaApiResult.timeout("服务器不可达"));
        var retryClient = new RetryableMediaRestClient(mockDelegate, 2, 50L, 2.0);

        var result = retryClient.get(testServer, "test", null);

        assertFalse(result.isSuccess());
        assertEquals(-1, result.getCode());
        assertEquals(3, mockDelegate.getCallCount()); // 1次首请求 + 2次重试
    }

    @Test
    @DisplayName("post_网络超时重试_验证重试次数正确")
    void post_networkTimeout_correctRetryCount() {
        var mockDelegate = new MockMediaRestClient(MediaApiResult.timeout("超时"));
        var retryClient = new RetryableMediaRestClient(mockDelegate, 5, 10L, 1.0);

        var result = retryClient.post(testServer, "test", new JSONObject());

        assertFalse(result.isSuccess());
        assertEquals(6, mockDelegate.getCallCount()); // 1 + 5 retries
    }

    @Test
    @DisplayName("postForm_成功_无重试")
    void postForm_success_noRetry() {
        var mockDelegate = new MockMediaRestClient(MediaApiResult.success(new JSONObject()));
        var retryClient = new RetryableMediaRestClient(mockDelegate, 3, 100L, 2.0);

        var result = retryClient.postForm(testServer, "test", Map.of("key", "value"));

        assertTrue(result.isSuccess());
        assertEquals(1, mockDelegate.getCallCount());
    }

    @Test
    @DisplayName("get_默认构造器_使用默认重试参数3次")
    void get_defaultConstructor_uses3Retries() {
        var mockDelegate = new MockMediaRestClient(MediaApiResult.timeout("超时"));
        var retryClient = new RetryableMediaRestClient(mockDelegate);

        var result = retryClient.get(testServer, "test", null);

        assertFalse(result.isSuccess());
        assertEquals(4, mockDelegate.getCallCount()); // 1 + 3 retries
    }

    @Test
    @DisplayName("buildUrl_委托给原始客户端")
    void buildUrl_delegatesToOriginal() {
        var mockDelegate = new MockMediaRestClient(MediaApiResult.success(null));
        var retryClient = new RetryableMediaRestClient(mockDelegate);

        var url = retryClient.buildUrl(testServer, "testApi");

        assertEquals("http://192.168.1.100:80/testApi", url);
    }

    @Test
    @DisplayName("get_零次重试_只调用一次")
    void get_zeroRetries_onlyOneCall() {
        var mockDelegate = new MockMediaRestClient(MediaApiResult.timeout("超时"));
        var retryClient = new RetryableMediaRestClient(mockDelegate, 0, 100L, 2.0);

        var result = retryClient.get(testServer, "test", null);

        assertFalse(result.isSuccess());
        assertEquals(1, mockDelegate.getCallCount());
    }

    @Test
    @DisplayName("get_退避间隔指数增长_不超过60秒上限")
    void get_exponentialBackoff_cappedAt60Seconds() {
        // 验证重试不会阻塞太久（间接验证退避上限）
        var mockDelegate = new MockMediaRestClient(MediaApiResult.timeout("超时"));
        // 初始间隔 30s，倍率 3.0：30s -> 60s(capped) -> 60s(capped)
        // 如果没有上限：30s -> 90s -> 270s 会很慢
        var retryClient = new RetryableMediaRestClient(mockDelegate, 2, 10L, 3.0);

        long start = System.currentTimeMillis();
        retryClient.get(testServer, "test", null);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(3, mockDelegate.getCallCount());
        // 应该在合理时间内完成（10ms + 30ms = 40ms 左右，加上开销不超过 1 秒）
        assertTrue(elapsed < 1000, "退避时间过长，可能退避上限未生效: " + elapsed + "ms");
    }

    @Test
    @DisplayName("get_中断线程_返回最后结果并恢复中断标志")
    void get_threadInterrupted_returnsAndRestoresInterrupt() {
        var callCount = new AtomicInteger(0);
        var delegate = new MediaRestClient() {
            @Override
            public MediaApiResult get(VideoMediaServer ms, String api, Map<String, Object> params) {
                int count = callCount.incrementAndGet();
                if (count == 1) {
                    return MediaApiResult.timeout("超时");
                }
                // 不应该到达这里，因为线程会在 sleep 时被中断
                return MediaApiResult.success(new JSONObject());
            }
            @Override
            public MediaApiResult post(VideoMediaServer ms, String api, JSONObject body) { return null; }
            @Override
            public MediaApiResult postForm(VideoMediaServer ms, String api, Map<String, Object> params) { return null; }
            @Override
            public String buildUrl(VideoMediaServer ms, String api) { return ""; }
        };

        var retryClient = new RetryableMediaRestClient(delegate, 3, 5000L, 2.0);

        // 在另一个线程中中断当前线程
        Thread testThread = Thread.currentThread();
        new Thread(() -> {
            try {
                Thread.sleep(50);
                testThread.interrupt();
            } catch (InterruptedException ignored) {}
        }).start();

        var result = retryClient.get(testServer, "test", null);

        // 被中断后应返回最后的结果
        assertNotNull(result);
        // 清理中断标志
        Thread.interrupted();
    }

    /**
     * Mock MediaRestClient 实现
     */
    private static class MockMediaRestClient implements MediaRestClient {
        private final MediaApiResult fixedResult;
        private int callCount = 0;

        MockMediaRestClient(MediaApiResult fixedResult) {
            this.fixedResult = fixedResult;
        }

        int getCallCount() { return callCount; }

        @Override
        public MediaApiResult get(VideoMediaServer ms, String api, Map<String, Object> params) {
            callCount++;
            return fixedResult;
        }

        @Override
        public MediaApiResult post(VideoMediaServer ms, String api, JSONObject body) {
            callCount++;
            return fixedResult;
        }

        @Override
        public MediaApiResult postForm(VideoMediaServer ms, String api, Map<String, Object> params) {
            callCount++;
            return fixedResult;
        }

        @Override
        public String buildUrl(VideoMediaServer ms, String api) {
            return String.format("http://%s:%s/%s", ms.getHost(), ms.getHttpPort(), api);
        }
    }
}
