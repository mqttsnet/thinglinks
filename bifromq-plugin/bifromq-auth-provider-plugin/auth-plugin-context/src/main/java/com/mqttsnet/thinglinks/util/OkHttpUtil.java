package com.mqttsnet.thinglinks.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 生产级 OkHttp 工具类. 异步优先,内置阶段计时 / 错误分类 / 拥塞监控 / 优雅关闭.
 * <p>核心:HTTP/1.1 多连接(绕开 H2 单连接瓶颈) + 5000 perHost + prestart 256 dispatcher 线程,
 * 10000+ TPS 稳态下亚毫秒响应.
 *
 * @author mqttsnet
 * @since 2026-05-16
 */
@Slf4j
public final class OkHttpUtil {

    // ============ 客户端容量============

    /**
     * 全局并发上限. 大于 perHost 提供突发缓冲
     */
    private static final int MAX_REQUESTS = 10000;
    /**
     * 单 host 并发上限. plugin 看到单 host(经 nginx/VIP),OkHttp 默认 5 是最大坑
     */
    private static final int MAX_REQUESTS_PER_HOST = 5000;
    /**
     * 连接池容量. 必须 ≥ {@link #MAX_REQUESTS_PER_HOST},否则 burst 后多余连接强制关闭引入抖动
     */
    private static final int CONNECTION_POOL_SIZE = 5000;
    /**
     * 连接保活分钟. 长保活减少握手抖动
     */
    private static final long KEEP_ALIVE_MINUTES = 15L;
    /**
     * Dispatcher 线程上限. ≥ perHost + 缓冲, SynchronousQueue 拒绝 = HTTP 调用直接失败
     */
    private static final int DISPATCHER_MAX_THREADS = 6144;
    /**
     * Dispatcher 常驻核心线程. 启动 prestart, burst 来时 0 spawn 延迟
     */
    private static final int DISPATCHER_CORE_THREADS = 256;
    /**
     * HTTP/2 PING 间隔秒. H1.1 时无效但不报错
     */
    private static final long PING_INTERVAL_SECONDS = 30L;

    // ============ 超时(严苛 fail-fast) ============

    /**
     * TCP 握手超时秒. 内网快速失败
     */
    private static final long CONNECT_TIMEOUT_SECONDS = 2L;
    /**
     * Socket 读超时秒. auth 应 < 100ms,3s 是兜底
     */
    private static final long READ_TIMEOUT_SECONDS = 3L;
    /**
     * Socket 写超时秒. payload 小
     */
    private static final long WRITE_TIMEOUT_SECONDS = 2L;
    /**
     * 整体调用超时秒. 防止挂死占 dispatcher 槽
     */
    private static final long CALL_TIMEOUT_SECONDS = 10L;

    // ============ 可观测性 ============

    /**
     * 慢请求阈值. auth 期望 < 50ms, 200ms 已异常
     */
    private static final long SLOW_REQUEST_THRESHOLD_MS = 200L;
    /**
     * Saturation 监控周期(秒)
     */
    private static final long SATURATION_LOG_INTERVAL_SECONDS = 30L;
    /**
     * 响应体最大读取字节. 防恶意/异常后端返回大体 OOM
     */
    private static final long MAX_RESPONSE_BYTES = 1L << 20;  // 1MB

    // ============ 单例 / 共享状态 ============

    /**
     * JSON MediaType 单例. 避免每次 RequestBody.create 走正则解析浪费 CPU
     */
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    /**
     * 全局请求头(并发安全). 通过 {@link #addGlobalHeader} 维护
     */
    private static final Map<String, String> GLOBAL_HEADERS = new ConcurrentHashMap<>();
    /**
     * Dispatcher 线程序号生成器
     */
    private static final AtomicInteger DISPATCHER_THREAD_SEQ = new AtomicInteger();
    /**
     * Saturation 采样调度器(单线程 daemon)
     */
    private static final ScheduledExecutorService SATURATION_MONITOR =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "okhttp-saturation-monitor");
            t.setDaemon(true);
            return t;
        });
    /**
     * 默认客户端,生命周期内不变
     */
    private static final OkHttpClient DEFAULT_CLIENT;
    /**
     * 自定义客户端覆盖(单元测试 / 特殊场景)
     */
    private static volatile OkHttpClient customClient;

    static {
        Dispatcher dispatcher = new Dispatcher(buildDispatcherExecutor());
        dispatcher.setMaxRequests(MAX_REQUESTS);
        dispatcher.setMaxRequestsPerHost(MAX_REQUESTS_PER_HOST);

        DEFAULT_CLIENT = new OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(new ConnectionPool(CONNECTION_POOL_SIZE, KEEP_ALIVE_MINUTES, TimeUnit.MINUTES))
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            // 强制 H1.1: H2 单 host 只 1 条 TCP, 5000 req/s 全挤一条 = 天花板;
            // H1.1 + connectionPool(5000) 真正用满 5000 条 TCP,实测 ~2400 → 5000+
            .protocols(List.of(Protocol.HTTP_1_1))
            .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .eventListenerFactory(call -> new PhaseTimingEventListener())
            .build();

        log.info("OkHttpUtil 初始化完成 - dispatcher max/perHost={}/{}, pool={}, keepAlive={}min, "
                + "core/maxThreads={}/{}, callTimeout={}s",
            MAX_REQUESTS, MAX_REQUESTS_PER_HOST, CONNECTION_POOL_SIZE, KEEP_ALIVE_MINUTES,
            DISPATCHER_CORE_THREADS, DISPATCHER_MAX_THREADS, CALL_TIMEOUT_SECONDS);

        startSaturationMonitor();
    }

    private OkHttpUtil() {
        // 工具类禁止实例化
    }

    // ============================================================
    //  公共 API: 配置
    // ============================================================

    /**
     * 注册全局请求头, 所有请求自动携带.
     *
     * @param key   头名称, 空值忽略
     * @param value 头值, 空值忽略
     */
    public static void addGlobalHeader(String key, String value) {
        if (key != null && value != null) {
            GLOBAL_HEADERS.put(key, value);
        }
    }

    /**
     * 移除全局请求头.
     *
     * @param key 头名称, 空值忽略
     */
    public static void removeGlobalHeader(String key) {
        if (key != null) {
            GLOBAL_HEADERS.remove(key);
        }
    }

    /**
     * 设置自定义 {@link OkHttpClient} 覆盖默认配置. 主要用于单元测试.
     *
     * @param client 自定义客户端, null 恢复默认
     */
    public static void setCustomClient(OkHttpClient client) {
        customClient = client;
    }

    /**
     * 优雅关闭. plugin 卸载或 JVM 退出时调用.
     * <p>顺序: 停 saturation 线程 → dispatcher 等已入队完成(≤5s) → 清空连接池 → 关 cache.
     */
    public static void shutdown() {
        log.info("OkHttpUtil 关闭中...");
        shutdownExecutor(SATURATION_MONITOR, 2);
        shutdownExecutor(DEFAULT_CLIENT.dispatcher().executorService(), 5);
        DEFAULT_CLIENT.connectionPool().evictAll();
        Optional.ofNullable(DEFAULT_CLIENT.cache()).ifPresent(c -> {
            try {
                c.close();
            } catch (IOException ignored) {
            }
        });
        log.info("OkHttpUtil 已优雅关闭");
    }

    // ============================================================
    //  公共 API: GET 同步
    // ============================================================

    /**
     * 同步 GET. 等价 {@link #sendGetRequest(String, Map, Map, ResponseConverter)} 不带参数和头.
     *
     * @param url       请求 URL
     * @param converter {@link ResponseConverter} 响应转换器
     * @param <T>       目标类型
     * @return Optional 包装结果, 空表示请求或转换失败
     * @throws IOException 网络异常
     */
    public static <T> Optional<T> sendGetRequest(String url, ResponseConverter<T> converter) throws IOException {
        return sendGetRequest(url, null, null, converter);
    }

    /**
     * 同步 GET 带查询参数.
     *
     * @param url       请求 URL
     * @param params    查询参数(可空)
     * @param converter {@link ResponseConverter} 响应转换器
     * @param <T>       目标类型
     * @return Optional 包装结果
     * @throws IOException 网络异常
     */
    public static <T> Optional<T> sendGetRequest(String url, Map<String, String> params,
                                                 ResponseConverter<T> converter) throws IOException {
        return sendGetRequest(url, params, null, converter);
    }

    /**
     * 同步 GET 带查询参数和请求头.
     *
     * @param url       请求 URL
     * @param params    查询参数(可空)
     * @param headers   自定义请求头(可空)
     * @param converter {@link ResponseConverter} 响应转换器
     * @param <T>       目标类型
     * @return Optional 包装结果
     * @throws IOException 网络异常
     */
    public static <T> Optional<T> sendGetRequest(String url, Map<String, String> params,
                                                 Map<String, String> headers,
                                                 ResponseConverter<T> converter) throws IOException {
        if (isBlank(url)) {
            return Optional.empty();
        }
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            log.error("[get] invalid url={}", url);
            return Optional.empty();
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        if (params != null) {
            params.forEach((k, v) -> {
                if (k != null && v != null) {
                    urlBuilder.addQueryParameter(k, v);
                }
            });
        }
        Request.Builder req = new Request.Builder().url(urlBuilder.build());
        applyAllHeaders(req, headers);
        try (Response response = getClient().newCall(req.build()).execute()) {
            return handleResponse(response, converter);
        }
    }

    // ============================================================
    //  公共 API: POST 同步
    // ============================================================

    /**
     * 同步 POST JSON. 等价 {@link #sendPostRequest(String, String, Map, ResponseConverter)} 不带头.
     *
     * @param url       请求 URL
     * @param jsonBody  JSON 请求体
     * @param converter {@link ResponseConverter} 响应转换器
     * @param <T>       目标类型
     * @return Optional 包装结果
     * @throws IOException 网络异常
     */
    public static <T> Optional<T> sendPostRequest(String url, String jsonBody,
                                                  ResponseConverter<T> converter) throws IOException {
        return sendPostRequest(url, jsonBody, null, converter);
    }

    /**
     * 同步 POST JSON 带请求头.
     *
     * @param url       请求 URL
     * @param jsonBody  JSON 请求体
     * @param headers   自定义请求头(可空)
     * @param converter {@link ResponseConverter} 响应转换器
     * @param <T>       目标类型
     * @return Optional 包装结果
     * @throws IOException 网络异常
     */
    public static <T> Optional<T> sendPostRequest(String url, String jsonBody,
                                                  Map<String, String> headers,
                                                  ResponseConverter<T> converter) throws IOException {
        if (isBlank(url) || jsonBody == null) {
            return Optional.empty();
        }
        try (Response response = getClient().newCall(buildJsonPost(url, jsonBody, headers)).execute()) {
            return handleResponse(response, converter);
        }
    }

    /**
     * 同步 POST JSON 仅返回 HTTP 状态码. 用于只关心 200/403 的 ACL 路径.
     *
     * @param url      请求 URL
     * @param jsonBody JSON 请求体
     * @param headers  自定义请求头(可空)
     * @return HTTP 状态码, 失败返回 -1
     * @throws IOException 网络异常
     */
    public static int sendPostRequestForStatus(String url, String jsonBody,
                                               Map<String, String> headers) throws IOException {
        if (isBlank(url) || jsonBody == null) {
            return -1;
        }
        try (Response response = getClient().newCall(buildJsonPost(url, jsonBody, headers)).execute()) {
            return response.code();
        }
    }

    // ============================================================
    //  公共 API: POST 异步(主推 - auth 高频路径)
    // ============================================================

    /**
     * 异步 POST JSON. 不阻塞调用方, 失败返回空 Optional 而非异常.
     * <p>配合 {@link CompletableFuture#thenApply} / {@link CompletableFuture#thenCompose} 链式编排.
     * <p>详细 timeline 由 {@link PhaseTimingEventListener} 自动输出, 无需调用方记录.
     *
     * @param url       请求 URL
     * @param jsonBody  JSON 请求体
     * @param headers   自定义请求头(可空)
     * @param converter {@link ResponseConverter} 响应转换器
     * @param <T>       目标类型
     * @return {@link CompletableFuture} 包装 Optional 结果
     */
    public static <T> CompletableFuture<Optional<T>> sendPostRequestAsync(String url,
                                                                          String jsonBody,
                                                                          Map<String, String> headers,
                                                                          ResponseConverter<T> converter) {
        CompletableFuture<Optional<T>> future = new CompletableFuture<>();
        if (isBlank(url) || jsonBody == null) {
            future.complete(Optional.empty());
            return future;
        }
        long startMs = System.currentTimeMillis();
        getClient().newCall(buildJsonPost(url, jsonBody, headers)).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                long elapsed = System.currentTimeMillis() - startMs;
                try (Response r = response) {
                    if (!r.isSuccessful()) {
                        log.warn("[auth-bad-status][{}] url={} elapsed={}ms", r.code(), url, elapsed);
                    }
                    future.complete(handleResponse(r, converter));
                } catch (Exception e) {
                    log.error("[auth-fail][response_parse] url={} elapsed={}ms err={}",
                        url, elapsed, e.getMessage(), e);
                    future.complete(Optional.empty());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                long elapsed = System.currentTimeMillis() - startMs;
                log.error("[auth-fail][{}] url={} elapsed={}ms err={}",
                    classifyError(e), url, elapsed, e.getMessage());
                future.complete(Optional.empty());
            }
        });
        return future;
    }

    /**
     * 异步 POST JSON 仅返回 HTTP 状态码. ACL 检查等只关心状态码场景.
     *
     * @param url      请求 URL
     * @param jsonBody JSON 请求体
     * @param headers  自定义请求头(可空)
     * @return {@link CompletableFuture} 包装状态码, 失败返回 -1
     */
    public static CompletableFuture<Integer> sendPostRequestForStatusAsync(String url,
                                                                           String jsonBody,
                                                                           Map<String, String> headers) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (isBlank(url) || jsonBody == null) {
            future.complete(-1);
            return future;
        }
        getClient().newCall(buildJsonPost(url, jsonBody, headers)).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try (Response r = response) {
                    future.complete(r.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                log.error("[acl-fail][{}] url={} err={}", classifyError(e), url, e.getMessage());
                future.complete(-1);
            }
        });
        return future;
    }

    // ============================================================
    //  内部辅助
    // ============================================================

    /**
     * 取当前生效客户端, 优先 customClient 否则 default.
     *
     * @return 当前 {@link OkHttpClient}
     */
    private static OkHttpClient getClient() {
        return customClient != null ? customClient : DEFAULT_CLIENT;
    }

    /**
     * 字符串空白判断.
     *
     * @param s 待检查字符串
     * @return null 或纯空白返回 true
     */
    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /**
     * 构建标准 JSON POST {@link Request}, 自动应用全局 + 自定义请求头.
     *
     * @param url      请求 URL
     * @param jsonBody JSON 请求体
     * @param headers  自定义请求头(可空)
     * @return 构建好的 {@link Request}
     */
    private static Request buildJsonPost(String url, String jsonBody, Map<String, String> headers) {
        Request.Builder req = new Request.Builder()
            .url(url)
            .post(RequestBody.create(jsonBody, JSON_MEDIA_TYPE));
        applyAllHeaders(req, headers);
        return req.build();
    }

    /**
     * 应用全局 + 自定义请求头到 builder.
     *
     * @param builder {@link Request.Builder}
     * @param headers 自定义请求头(可空)
     */
    private static void applyAllHeaders(Request.Builder builder, Map<String, String> headers) {
        GLOBAL_HEADERS.forEach((k, v) -> {
            if (k != null && v != null) {
                builder.addHeader(k, v);
            }
        });
        if (headers != null) {
            headers.forEach((k, v) -> {
                if (k != null && v != null) {
                    builder.addHeader(k, v);
                }
            });
        }
    }

    /**
     * 统一响应处理: HTTP 状态校验 → 大小限制 → 转换器转换. 任意阶段失败返回空 Optional.
     * <p>使用 {@link Response#peekBody(long)} 限制最大读取字节, 防恶意大响应 OOM.
     *
     * @param response  HTTP 响应
     * @param converter {@link ResponseConverter} 响应转换器
     * @param <T>       目标类型
     * @return Optional 包装结果
     */
    private static <T> Optional<T> handleResponse(Response response, ResponseConverter<T> converter) {
        if (response == null || !response.isSuccessful() || response.body() == null || converter == null) {
            return Optional.empty();
        }
        try {
            String body = response.peekBody(MAX_RESPONSE_BYTES).string();
            if (body.isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(converter.convert(body));
        } catch (Exception e) {
            log.error("[response-parse] code={} err={}", response.code(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * {@link Throwable} → 错误类型标签. 监控按 type 聚合.
     *
     * @param e 异常
     * @return 错误类型短串(read_timeout / connect_refused / dns_failure / ssl_failure 等)
     */
    private static String classifyError(Throwable e) {
        if (e == null) {
            return "unknown";
        }
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (e instanceof SocketTimeoutException) {
            if (msg.contains("connect")) {
                return "connect_timeout";
            }
            if (msg.contains("read")) {
                return "read_timeout";
            }
            return "timeout";
        }
        if (e instanceof ConnectException) {
            return "connect_refused";
        }
        if (e instanceof UnknownHostException) {
            return "dns_failure";
        }
        if (e instanceof SSLException) {
            return "ssl_failure";
        }
        if (e instanceof InterruptedIOException) {
            return msg.contains("canceled") ? "canceled" : "interrupted";
        }
        if (msg.contains("timeout") || msg.contains("timed out")) {
            return "timeout";
        }
        if (msg.contains("reset")) {
            return "connection_reset";
        }
        if (msg.contains("closed")) {
            return "connection_closed";
        }
        return "unknown";
    }

    /**
     * 优雅关闭一个 {@link ExecutorService}. timeout 内未结束则强制 shutdownNow.
     *
     * @param exec    待关闭的 executor
     * @param seconds 超时秒
     */
    private static void shutdownExecutor(ExecutorService exec, long seconds) {
        if (exec == null) {
            return;
        }
        exec.shutdown();
        try {
            if (!exec.awaitTermination(seconds, TimeUnit.SECONDS)) {
                exec.shutdownNow();
            }
        } catch (InterruptedException e) {
            exec.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Dispatcher 专用执行器. core=256 prestart + max=6144 弹性扩容,
     * {@link SynchronousQueue} 直接交接(无缓冲队列).
     *
     * @return {@link ExecutorService}
     */
    private static ExecutorService buildDispatcherExecutor() {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(
            DISPATCHER_CORE_THREADS, DISPATCHER_MAX_THREADS,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            r -> {
                Thread t = new Thread(r, "okhttp-auth-dispatcher-" + DISPATCHER_THREAD_SEQ.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        );
        exec.allowCoreThreadTimeOut(false);
        exec.prestartAllCoreThreads();
        return exec;
    }

    /**
     * 周期采样 dispatcher / connection pool 状态. queued > 1000 = 上游堵塞预警.
     */
    private static void startSaturationMonitor() {
        SATURATION_MONITOR.scheduleAtFixedRate(() -> {
            try {
                Dispatcher d = DEFAULT_CLIENT.dispatcher();
                ConnectionPool p = DEFAULT_CLIENT.connectionPool();
                int running = d.runningCallsCount();
                int queued = d.queuedCallsCount();
                if (queued > 1000 || running >= MAX_REQUESTS_PER_HOST * 0.9) {
                    log.warn("[okhttp-saturation] running={}/{} queued={} | conn idle={}/{} ⚠️ 接近上限",
                        running, MAX_REQUESTS_PER_HOST, queued, p.idleConnectionCount(), p.connectionCount());
                } else if (running > 0 || queued > 0) {
                    log.info("[okhttp-saturation] running={}/{} queued={} | conn idle={}/{}",
                        running, MAX_REQUESTS_PER_HOST, queued, p.idleConnectionCount(), p.connectionCount());
                }
            } catch (Throwable t) {
                log.error("[okhttp-saturation] 采样异常", t);
            }
        }, SATURATION_LOG_INTERVAL_SECONDS, SATURATION_LOG_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    // ============================================================
    //  阶段计时 EventListener (内部类)
    // ============================================================

    /**
     * 阶段计时 {@link EventListener}. 失败时输出完整 timeline 定位卡点 (DNS/TCP/TLS/wait);
     * 成功超过 {@link #SLOW_REQUEST_THRESHOLD_MS} 打 WARN.
     */
    private static final class PhaseTimingEventListener extends EventListener {
        private long callStartNs = -1;
        private long dnsStartNs = -1;
        private long dnsEndNs = -1;
        private long connectStartNs = -1;
        private long connectEndNs = -1;
        private long secureStartNs = -1;
        private long secureEndNs = -1;
        private long requestSentNs = -1;
        private long responseStartNs = -1;

        @Override
        public void callStart(Call call) {
            callStartNs = System.nanoTime();
        }

        @Override
        public void dnsStart(Call call, String domainName) {
            dnsStartNs = System.nanoTime();
        }

        @Override
        public void dnsEnd(Call call, String domainName, List<InetAddress> addrs) {
            dnsEndNs = System.nanoTime();
        }

        @Override
        public void connectStart(Call call, InetSocketAddress addr, Proxy proxy) {
            connectStartNs = System.nanoTime();
        }

        @Override
        public void connectEnd(Call call, InetSocketAddress addr, Proxy proxy, Protocol protocol) {
            connectEndNs = System.nanoTime();
        }

        @Override
        public void secureConnectStart(Call call) {
            secureStartNs = System.nanoTime();
        }

        @Override
        public void secureConnectEnd(Call call, Handshake handshake) {
            secureEndNs = System.nanoTime();
        }

        @Override
        public void requestHeadersEnd(Call call, Request request) {
            requestSentNs = System.nanoTime();
        }

        @Override
        public void responseHeadersStart(Call call) {
            responseStartNs = System.nanoTime();
        }

        @Override
        public void callEnd(Call call) {
            long total = ms(callStartNs, System.nanoTime());
            if (total > SLOW_REQUEST_THRESHOLD_MS) {
                log.warn("[okhttp-slow] url={} {}", call.request().url(), timeline(total));
            } else if (log.isTraceEnabled()) {
                log.trace("[okhttp-ok] url={} {}", call.request().url(), timeline(total));
            }
        }

        @Override
        public void callFailed(Call call, IOException ioe) {
            long total = ms(callStartNs, System.nanoTime());
            log.error("[okhttp-fail][{}] url={} elapsed={}ms err={} timeline={{{}}}",
                classifyError(ioe), call.request().url(), total, ioe.getMessage(), timeline(total));
        }

        /**
         * 纳秒区间转毫秒. 起始时间 ≤ 0 视为未发生, 返回 -1.
         *
         * @param fromNs 起始 ns
         * @param toNs   结束 ns
         * @return 毫秒差 (或 -1)
         */
        private long ms(long fromNs, long toNs) {
            return fromNs <= 0 ? -1 : (toNs - fromNs) / 1_000_000;
        }

        /**
         * 拼接阶段耗时 timeline, 跳过未发生阶段.
         *
         * @param totalMs 总耗时
         * @return 类似 "total=312ms dns=2ms tcp=5ms tls=14ms wait=305ms"
         */
        private String timeline(long totalMs) {
            StringBuilder sb = new StringBuilder(96).append("total=").append(totalMs).append("ms");
            if (dnsEndNs > 0) {
                sb.append(" dns=").append(ms(dnsStartNs, dnsEndNs)).append("ms");
            }
            if (connectEndNs > 0) {
                sb.append(" tcp=").append(ms(connectStartNs, connectEndNs)).append("ms");
            }
            if (secureEndNs > 0) {
                sb.append(" tls=").append(ms(secureStartNs, secureEndNs)).append("ms");
            }
            if (responseStartNs > 0) {
                sb.append(" wait=").append(ms(requestSentNs, responseStartNs)).append("ms");
            } else if (requestSentNs > 0) {
                sb.append(" wait=>").append(ms(requestSentNs, System.nanoTime())).append("ms");
            }
            return sb.toString();
        }
    }

    /**
     * 响应体转换器. 将 HTTP body 字符串转换为目标类型.
     *
     * @param <T> 目标类型
     */
    @FunctionalInterface
    public interface ResponseConverter<T> {
        /**
         * 转换响应体.
         *
         * @param responseBody 响应体字符串
         * @return 转换后对象, null 视为转换失败
         */
        T convert(String responseBody);
    }
}
