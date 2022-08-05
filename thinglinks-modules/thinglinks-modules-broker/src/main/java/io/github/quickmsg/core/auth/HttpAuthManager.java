package io.github.quickmsg.core.auth;

import io.github.quickmsg.common.auth.AuthManager;
import io.github.quickmsg.common.config.AuthConfig;
import io.github.quickmsg.common.utils.JacksonUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author luxurong
 */
public class HttpAuthManager implements AuthManager {

    private final AuthConfig authConfig;

    private final HttpClient client;

    public HttpAuthManager(AuthConfig authConfig) {
        this.authConfig = authConfig;
        AuthConfig.HttpAuthConfig httpAuthConfig = authConfig.getHttp();
        this.client = HttpClient.create().host(httpAuthConfig.getHost()).port(httpAuthConfig.getPort())
                .headers(headers -> {
                    headers.add(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json;utf-8");
                    Optional.ofNullable(httpAuthConfig.getHeaders())
                            .ifPresent(addHeaders -> addHeaders.forEach(headers::add));
                });
    }
    @Override
    public Boolean auth(String userName, byte[] passwordInBytes, String clientIdentifier) {
        AuthConfig.HttpAuthConfig httpAuthConfig = authConfig.getHttp();
        Map<String, String> params = new HashMap<>();
        params.put("clientIdentifier", clientIdentifier);
        params.put("username", userName != null ? userName : "");
        params.put("password", passwordInBytes != null ? new String(passwordInBytes, StandardCharsets.UTF_8):"");
        params.putAll(httpAuthConfig.getParams());
        return client.post().uri(httpAuthConfig.getPath())
                .send(ByteBufFlux.fromString(Mono.just(JacksonUtil.map2Json(params))))
                .response()
                .map(response -> HttpResponseStatus.OK.code() == response.status().code())
                .block(Duration.ofSeconds(3));
    }

}
