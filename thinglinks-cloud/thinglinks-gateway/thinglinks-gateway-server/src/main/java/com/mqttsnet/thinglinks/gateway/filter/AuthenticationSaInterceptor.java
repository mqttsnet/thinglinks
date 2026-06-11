package com.mqttsnet.thinglinks.gateway.filter;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.reactor.context.SaReactorHolder;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.spring.pathmatch.SaPathPatternParserUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.common.properties.IgnoreProperties;
import com.mqttsnet.thinglinks.model.vo.result.ResourceApiVO;
import com.mqttsnet.thinglinks.gateway.cache.ResourceApiLocalCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *注册 Sa-Token全局过滤器
 * @author tangyh
 * @since 2024/8/6 16:33
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationSaInterceptor implements WebFilter, Ordered {
    private final ResourceApiLocalCache resourceApiLocalCache;
    private final IgnoreProperties ignoreProperties;

    @Override
    public int getOrder() {
        return OrderedConstant.AUTHENTICATION;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // 写入WebFilterChain对象
        exchange.getAttributes().put(SaReactorHolder.EXCHANGE_KEY, chain);
//        if (true) {
//            return chain.filter(exchange).contextWrite(ctx -> {
//                ctx = ctx.put(SaReactorHolder.CHAIN_KEY, exchange);
//                return ctx;
//            }).doFinally(r -> {
//                SaReactorSyncHolder.clearContext();
//            });
//        }

        // ---------- 全局认证处理
        try {
            // 写入全局上下文 (同步)
            SaReactorSyncHolder.setContext(exchange);

            // 执行全局过滤器

            Map<String, Set<String>> anyUser = ignoreProperties.buildAnyUser();
            // 验证token 排除掉需要租户ID，但不需要登录
            SaRouter
                    .match("/**")    // 拦截的 path 列表，可以写多个 */
                    .notMatch(r -> {
                        String path = SaHolder.getRequest().getRequestPath();
                        String method = SaHolder.getRequest().getMethod();
                        for (Map.Entry<String, Set<String>> map : anyUser.entrySet()) {
                            String key = map.getKey();
                            Set<String> value = map.getValue();
                            if (StrUtil.equalsAny(key, method, SaHttpMethod.ALL.name())) {
                                for (String ignore : value) {
                                    if (StrUtil.equals(ignore, path)) {
                                        return true;
                                    }

                                    if (SaPathPatternParserUtil.match(ignore, path)) {
                                        return true;
                                    }
                                }
                            }
                        }
                        return false;
                    })
                    .check(r -> StpUtil.checkLogin());

            // 无需校验权限
            if (!ignoreProperties.getAuthEnabled()) {
                return chain.filter(exchange).contextWrite(ctx -> {
                    ctx = ctx.put(SaReactorHolder.EXCHANGE_KEY, exchange);
                    return ctx;
                }).doFinally(r -> {
                    SaReactorSyncHolder.clearContext();
                });
            }

            // 接口权限:本地缓存取表(零 Redis),anyone 判定提到循环外算一次
            // 等价原每 api 内联判断;命中(如 login)整段跳过,免去 N 次空匹配
            Map<String, Set<String>> anyone = ignoreProperties.buildAnyone();
            Map<String, Set<String>> allApi = resourceApiLocalCache.getAllApi();
            String reqPath = SaHolder.getRequest().getRequestPath();
            String reqMethod = SaHolder.getRequest().getMethod();

            if (!isAnyonePermitted(anyone, reqMethod, reqPath)) {
                allApi.forEach((api, auth) -> {
                    List<String> list = StrUtil.split(api, "###");
                    String uri = list.get(0);
                    String requestMethod = list.get(1);
                    SaRouter.match(uri).matchMethod(requestMethod)
                            .check(r -> StpUtil.checkPermissionOr(auth.toArray(String[]::new)));
                });
            }


            if (!ignoreProperties.getNotConfigUriAllow()) {
                String path = SaHolder.getRequest().getRequestPath();
                String method = SaHolder.getRequest().getMethod();
                ResourceApiVO resourceApi = new ResourceApiVO();
                resourceApi.setUri(path);
                resourceApi.setRequestMethod(method);


                if (!ignoreProperties.isIgnoreAnyone(method, path)) {
                    boolean flag = false;
                    for (Map.Entry<String, Set<String>> map : allApi.entrySet()) {
                        List<String> list = StrUtil.split(map.getKey(), "###");
                        String uri = list.get(0);
                        String requestMethod = list.get(1);

                        if (StrUtil.equalsAny(requestMethod, method, SaHttpMethod.ALL.name())) {
                            if (StrUtil.equals(uri, path) || SaPathPatternParserUtil.match(uri, path)) {
                                flag = true;
                            }
                        }
                    }

                    if (!flag) {
                        throw new NotPermissionException(resourceApi.getUri(), StpUtil.TYPE).setCode(SaErrorCode.CODE_11051);
                    }
                }
            }

        } catch (StopMatchException e) {
            log.error(e.getMessage(), e);
            // StopMatchException 异常代表：停止匹配，进入Controller

        } catch (SaTokenException e) {
            String result = e.getMessage();
            log.error(e.getMessage(), e);
            ServerHttpResponse response = exchange.getResponse();
            R tokenError = R.fail(e.getCode(), result);
            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//            response.setStatusCode(HttpStatus.BAD_REQUEST);
            DataBuffer dataBuffer = response.bufferFactory().wrap(tokenError.toString().getBytes());
            return response.writeWith(Mono.just(dataBuffer));
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            // 1. 获取异常处理策略结果
            String result = e.getMessage();
            ServerHttpResponse response = exchange.getResponse();
            R tokenError = R.fail(result);
            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            DataBuffer dataBuffer = response.bufferFactory().wrap(tokenError.toString().getBytes());
            return response.writeWith(Mono.just(dataBuffer));
        } finally {
            // 清除上下文
            SaReactorSyncHolder.clearContext();
        }

        // ---------- 执行

        // 写入全局上下文 (同步)
        SaReactorSyncHolder.setContext(exchange);

        // 执行
        return chain.filter(exchange).contextWrite(ctx -> {
            // 写入全局上下文 (异步)
            ctx = ctx.put(SaReactorHolder.EXCHANGE_KEY, exchange);
            return ctx;
        }).doFinally(r -> {
            // 清除上下文
            SaReactorSyncHolder.clearContext();
        });
    }

    /**
     * 路径是否命中 anyone 免鉴权名单 ── 遍历全量(区别于只判首项的 {@code isIgnoreAnyone}),
     * 等价原 Loop1 内联判断,提到循环外算一次。
     *
     * @param anyone 免鉴权映射(method → paths)
     * @param method 请求方法
     * @param path   请求路径
     * @return 命中返 {@code true}(跳过权限校验)
     */
    private boolean isAnyonePermitted(Map<String, Set<String>> anyone, String method, String path) {
        for (Map.Entry<String, Set<String>> entry : anyone.entrySet()) {
            if (StrUtil.equalsAny(entry.getKey(), method, SaHttpMethod.ALL.name())) {
                for (String ignore : entry.getValue()) {
                    if (StrUtil.equals(ignore, path) || SaPathPatternParserUtil.match(ignore, path)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
