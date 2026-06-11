package com.mqttsnet.thinglinks.video.media.abl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.media.common.MediaApiResult;
import com.mqttsnet.thinglinks.video.media.common.MediaRestClient;
import com.mqttsnet.thinglinks.video.media.common.MediaRestConfig;
import com.mqttsnet.thinglinks.video.utils.MediaUrlUtils;
import com.mqttsnet.thinglinks.video.media.common.RetryableMediaRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;

/**
 * Description:
 * ABLMediaServer REST 客户端实现。
 * 封装对 ABLMediaServer 最新版本 HTTP API 的调用，
 * 所有请求自动携带 secret 鉴权参数。
 * 内置重试机制，支持配置化的重试次数和退避策略。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class AblRestClient implements MediaRestClient {

    private final RestTemplate restTemplate;
    private final RetryableMediaRestClient retryClient;

    public AblRestClient(MediaRestConfig config) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(config.getConnectTimeout());
        factory.setReadTimeout(config.getReadTimeout());
        this.restTemplate = new RestTemplate(factory);
        this.retryClient = new RetryableMediaRestClient(
                new InternalClient(), config.getRetryMaxAttempts(),
                config.getRetryInitialInterval(), config.getRetryMultiplier());
        log.info("AblRestClient初始化完成: connectTimeout={}ms, readTimeout={}ms, 重试={}次",
                config.getConnectTimeout(), config.getReadTimeout(), config.getRetryMaxAttempts());
    }

    @Override
    public MediaApiResult get(VideoMediaServer mediaServer, String api, Map<String, Object> params) {
        log.debug("[ABL] GET请求: server={}:{}, api={}", mediaServer.getHost(), mediaServer.getHttpPort(), api);
        return retryClient.get(mediaServer, api, params);
    }

    @Override
    public MediaApiResult post(VideoMediaServer mediaServer, String api, JSONObject body) {
        log.debug("[ABL] POST请求: server={}:{}, api={}", mediaServer.getHost(), mediaServer.getHttpPort(), api);
        return retryClient.post(mediaServer, api, body);
    }

    @Override
    public MediaApiResult postForm(VideoMediaServer mediaServer, String api, Map<String, Object> params) {
        log.debug("[ABL] POST_FORM请求: server={}:{}, api={}", mediaServer.getHost(), mediaServer.getHttpPort(), api);
        return retryClient.postForm(mediaServer, api, params);
    }

    @Override
    public String buildUrl(VideoMediaServer mediaServer, String api) {
        return MediaUrlUtils.buildApiUrl(mediaServer.getHost(), mediaServer.getHttpPort(), "index/api/" + api);
    }

    /**
     * 构建带 secret 的 URL Builder
     */
    private UriComponentsBuilder buildUrlWithSecret(VideoMediaServer mediaServer, String api) {
        var url = buildUrl(mediaServer, api);
        var builder = UriComponentsBuilder.fromHttpUrl(url);
        if (StrUtil.isNotBlank(mediaServer.getSecret())) {
            builder.queryParam("secret", mediaServer.getSecret());
        }
        return builder;
    }

    /**
     * 执行 HTTP GET 请求并解析响应
     */
    private MediaApiResult executeGet(String url, VideoMediaServer mediaServer) {
        try {
            log.debug("[ABL] 发送GET请求: url={}", url);
            var responseStr = restTemplate.getForObject(url, String.class);
            return parseResponse(url, responseStr, mediaServer);
        } catch (ResourceAccessException e) {
            log.error("[ABL] 服务器连接失败 [{}:{}] url={}, error={}",
                    mediaServer.getHost(), mediaServer.getHttpPort(), url, e.getMessage());
            return MediaApiResult.timeout("ABL服务器连接超时: " + e.getMessage());
        } catch (Exception e) {
            log.error("[ABL] API调用异常 url={}, error={}", url, e.getMessage(), e);
            return MediaApiResult.fail(-2, "ABL API调用异常: " + e.getMessage());
        }
    }

    /**
     * 执行 HTTP POST 请求并解析响应
     */
    private MediaApiResult executePost(String url, VideoMediaServer mediaServer) {
        try {
            log.debug("[ABL] 发送POST请求: url={}", url);
            var responseStr = restTemplate.postForObject(url, null, String.class);
            return parseResponse(url, responseStr, mediaServer);
        } catch (ResourceAccessException e) {
            log.error("[ABL] 服务器连接失败 [{}:{}] url={}, error={}",
                    mediaServer.getHost(), mediaServer.getHttpPort(), url, e.getMessage());
            return MediaApiResult.timeout("ABL服务器连接超时: " + e.getMessage());
        } catch (Exception e) {
            log.error("[ABL] API调用异常 url={}, error={}", url, e.getMessage(), e);
            return MediaApiResult.fail(-2, "ABL API调用异常: " + e.getMessage());
        }
    }

    /**
     * 解析响应内容
     */
    private MediaApiResult parseResponse(String url, String responseStr, VideoMediaServer mediaServer) {
        if (StrUtil.isBlank(responseStr)) {
            log.warn("[ABL] 响应为空: url={}", url);
            return MediaApiResult.fail(-2, "ABL响应为空");
        }
        var json = JSON.parseObject(responseStr);
        if (json == null) {
            log.warn("[ABL] 响应JSON解析失败: url={}, body={}", url, responseStr);
            return MediaApiResult.fail(-2, "ABL响应JSON解析失败");
        }
        int code = json.getIntValue("code");
        var result = MediaApiResult.builder()
                .code(code)
                .msg(code == 0 ? "success" : json.getString("msg"))
                .data(json)
                .rawBody(responseStr)
                .build();
        if (code != 0) {
            log.warn("[ABL] API返回错误: url={}, code={}, msg={}", url, code, result.getMsg());
        }
        return result;
    }

    /**
     * 内部客户端：不带重试的核心请求逻辑，供 RetryableMediaRestClient 包装
     */
    private class InternalClient implements MediaRestClient {
        @Override
        public MediaApiResult get(VideoMediaServer mediaServer, String api, Map<String, Object> params) {
            var url = buildUrlWithSecret(mediaServer, api);
            if (params != null) {
                params.forEach((key, value) -> url.queryParam(key, Objects.toString(value, "")));
            }
            return executeGet(url.toUriString(), mediaServer);
        }

        @Override
        public MediaApiResult post(VideoMediaServer mediaServer, String api, JSONObject body) {
            var url = buildUrlWithSecret(mediaServer, api);
            if (body != null) {
                body.forEach((key, value) -> url.queryParam(key, Objects.toString(value, "")));
            }
            return executePost(url.toUriString(), mediaServer);
        }

        @Override
        public MediaApiResult postForm(VideoMediaServer mediaServer, String api, Map<String, Object> params) {
            var url = buildUrlWithSecret(mediaServer, api);
            if (params != null) {
                params.forEach((key, value) -> url.queryParam(key, Objects.toString(value, "")));
            }
            return executePost(url.toUriString(), mediaServer);
        }

        @Override
        public String buildUrl(VideoMediaServer mediaServer, String api) {
            return AblRestClient.this.buildUrl(mediaServer, api);
        }
    }
}
