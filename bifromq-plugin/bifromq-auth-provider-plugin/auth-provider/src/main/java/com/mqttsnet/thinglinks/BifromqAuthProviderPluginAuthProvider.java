/*
 * Copyright (c) 2024. The BifroMQ Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.mqttsnet.thinglinks;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baidu.bifromq.plugin.authprovider.IAuthProvider;
import com.baidu.bifromq.plugin.authprovider.type.Failed;
import com.baidu.bifromq.plugin.authprovider.type.MQTT3AuthData;
import com.baidu.bifromq.plugin.authprovider.type.MQTT3AuthResult;
import com.baidu.bifromq.plugin.authprovider.type.MQTT5AuthData;
import com.baidu.bifromq.plugin.authprovider.type.MQTT5AuthResult;
import com.baidu.bifromq.plugin.authprovider.type.MQTT5ExtendedAuthData;
import com.baidu.bifromq.plugin.authprovider.type.MQTT5ExtendedAuthResult;
import com.baidu.bifromq.plugin.authprovider.type.MQTTAction;
import com.baidu.bifromq.plugin.authprovider.type.Ok;
import com.baidu.bifromq.plugin.authprovider.type.Reject;
import com.baidu.bifromq.plugin.authprovider.type.Success;
import com.baidu.bifromq.type.ClientInfo;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.topic.MqttTopicMatcher;
import com.mqttsnet.thinglinks.config.acl.AclCacheConfig;
import com.mqttsnet.thinglinks.config.threadpool.ThreadPoolConfig;
import com.mqttsnet.thinglinks.constant.CommonConstants;
import com.mqttsnet.thinglinks.entity.acl.DeviceAclRule;
import com.mqttsnet.thinglinks.entity.config.AuthProviderConfig;
import com.mqttsnet.thinglinks.entity.config.PluginConfig;
import com.mqttsnet.thinglinks.entity.device.DeviceInfo;
import com.mqttsnet.thinglinks.entity.enumeration.ClientAclActionTypeEnum;
import com.mqttsnet.thinglinks.entity.enumeration.DeviceAclRuleActionTypeEnum;
import com.mqttsnet.thinglinks.util.AclTopicPatternPlaceholderReplacer;
import com.mqttsnet.thinglinks.util.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;
import org.springframework.http.HttpStatus;

/**
 * -----------------------------------------------------------------------------
 * File Name: BifromqAuthProviderPluginAuthProvider
 * -----------------------------------------------------------------------------
 * Description:
 * <a href="https://bifromq.apache.org/docs/plugin/auth_provider/">...</a>
 * Auth Provider插件旨在为BifroMQ运行时提供验证MQTT客户端连接和授权发布/订阅消息主题的能力
 * 认证提供者插件，用于支持 MQTT3 和 MQTT5 客户端的认证。
 * 支持基于 HTTP 请求的客户端身份验证，能够解析 ACL（访问控制列表）信息并返回给客户端。
 * 通过 {@link BifromqAuthProviderContext} 获取插件的配置信息。
 * <p>
 * 1. 实现IAuthProvider接口
 * 2. 通过@Extension注解标记为插件
 * 3. 实现auth方法，调用ThingLinks 的认证接口验证客户端连接
 * 4. 实现check方法，验证客户端是否有权限执行指定的操作
 * <p>
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/2/23       mqttsnet        1.0        Initial creation
 * 2025/5/10       mqttsnet        1.0        ACL鉴权支持
 * -----------------------------------------------------------------------------
 * @email mqttsnet@163.com
 * @date 2024/2/23 15:36
 */
@Slf4j
@Extension
public final class BifromqAuthProviderPluginAuthProvider implements IAuthProvider, AutoCloseable {

    /**
     * topic 归一化用的多斜杠合并 Pattern,预编译避免 replaceAll 每次重新编译正则.
     */
    private static final Pattern MULTI_SLASH_PATTERN = java.util.regex.Pattern.compile("/+");
    private final AtomicBoolean stopped = new AtomicBoolean();
    private final ThreadPoolExecutor executor;
    private final AuthProviderConfig.AuthConfig authConfig;
    private final AuthProviderConfig.AclConfig aclConfig;
    /**
     * ACL 异步缓存,内置并发 miss dedup,防缓存击穿.
     */
    private final AsyncCache<CacheKey, Boolean> aclCache;

    /**
     * 构造函数，通过 {@link BifromqAuthProviderContext} 初始化配置。
     *
     * @param context {@link BifromqAuthProviderContext} 认证插件的上下文，包含配置信息。
     */
    public BifromqAuthProviderPluginAuthProvider(BifromqAuthProviderContext context) {
        // 通过 context 获取配置
        PluginConfig pluginConfig = context.getPluginConfig();
        AuthProviderConfig providerConfig = pluginConfig.getAuthProviderConfig();

        this.authConfig = providerConfig.getAuth();
        this.aclConfig = providerConfig.getAcl();

        log.info("认证服务地址: {}", authConfig.getClientAuthUrl());
        log.info("ACL 服务状态: {}", aclConfig.isEnabled() ? "已启用" : "已禁用");
        log.info("AuthProvider Config: clientConnectionUrl={}, aclCheckUrl={}", authConfig.getClientAuthUrl(), aclConfig.getAclCheckUrl());

        // 初始化线程池
        // ★ async 化后 executor 只做 CPU 工作(JSON 解析/Ok 构建/ACL 匹配),HTTP 全在 OkHttp dispatcher.
        // 倍数 50/200(高水位配置):16 核 = core=800/max=3200,32 核 = core=1600/max=6400,queue=10000.
        // ThreadPoolConfig.newFixedExecutor 默认 allowCoreThreadTimeOut=false,core 线程常驻,
        // bursty 流量来时无 spawn 开销直接接;突发超 core 时弹性扩容到 max,超出部分 60s 自动回收.
        this.executor = ThreadPoolConfig.newFixedExecutor(
            "auth-worker",
            Runtime.getRuntime().availableProcessors() * 50,
            Runtime.getRuntime().availableProcessors() * 200,
            10000
        );

        // 初始化ACL 缓存
        this.aclCache = AclCacheConfig.buildCache(aclConfig.getCache(), executor);
    }

    /**
     * 按 ACL 规则列表检查 topic 是否允许 ── 直接调用 Util {@link MqttTopicMatcher} 做 MQTT 通配符匹配，
     * 本方法只承担 ACL 业务语义:
     * <ol>
     *   <li>按 priority 升序排序(数字越小优先级越高)</li>
     *   <li>过滤 enabled 规则</li>
     *   <li>找第一个 topic 命中的规则,返回其 decision</li>
     *   <li>未命中任何规则 → 默认拒绝(false)</li>
     * </ol>
     * 占位符已在调用方提前用 {@link AclTopicPatternPlaceholderReplacer} 完成替换。
     */
    private static boolean isTopicAllowedByRules(String topic, List<DeviceAclRule> rules) {
        if (StrUtil.isBlank(topic) || rules == null || rules.isEmpty()) {
            return false;
        }
        return rules.stream()
            .filter(Objects::nonNull)
            .filter(r -> Boolean.TRUE.equals(r.getEnabled()))
            .sorted(Comparator.comparingInt(DeviceAclRule::getPriority))
            .filter(r -> MqttTopicMatcher.match(r.getTopicPattern(), topic))
            .findFirst()
            .map(DeviceAclRule::getDecision)
            .orElse(false);
    }

    /**
     * MQTT3协议的认证方法，验证客户端连接请求。
     * <p>★ 异步版本: 使用 OkHttp.enqueue 不阻塞 executor 线程,
     * 响应解析在 executor 上完成(避免占 OkHttp dispatcher IO 线程).
     *
     * @param authData {@link MQTT3AuthData} 包含MQTT3认证数据
     * @return {@link CompletableFuture} 包含MQTT3认证结果 {@link MQTT3AuthResult}
     */
    @Override
    public CompletableFuture<MQTT3AuthResult> auth(MQTT3AuthData authData) {
        String clientId = authData.getClientId();
        String password = authData.getPassword().toStringUtf8();
        String username = authData.getUsername();
        String cert = authData.getCert().toStringUtf8();
        String remoteAddr = authData.getRemoteAddr();
        String channelId = authData.getChannelId();
        log.info("MQTT3 - Authenticating client - clientId: {}, username: {}, cert: {}", clientId, username, cert);

        return clientConnectionAuthenticationAsync(clientId, password, username, cert, remoteAddr, channelId)
            .thenApplyAsync(this::handleMQTT3AuthenticationResponse, executor);
    }

    /**
     * MQTT5协议的认证方法，验证客户端连接请求。
     * <p>★ 异步版本: 与 MQTT3 同套路.
     *
     * @param authData {@link MQTT5AuthData} 包含MQTT5认证数据
     * @return {@link CompletableFuture} 包含MQTT5认证结果 {@link MQTT5AuthResult}
     */
    @Override
    public CompletableFuture<MQTT5AuthResult> auth(MQTT5AuthData authData) {
        String clientId = authData.getClientId();
        String password = authData.getPassword().toStringUtf8();
        String username = authData.getUsername();
        String cert = authData.getCert().toStringUtf8();
        String remoteAddr = authData.getRemoteAddr();
        String channelId = authData.getChannelId();
        log.info("MQTT5 - Authenticating client - clientId: {}, username: {}, cert: {}", clientId, username, cert);

        return clientConnectionAuthenticationAsync(clientId, password, username, cert, remoteAddr, channelId)
            .thenApplyAsync(this::handleMQTT5AuthenticationResponse, executor);
    }

    /**
     * 异步向远程认证服务器发送POST请求.
     * <p>★ 核心改造: 用 {@link OkHttpUtil#sendPostRequestAsync} 替代 supplyAsync 包同步 .execute().
     * <p>请求构建本身极快(JSONObject 几个 put),直接在调用线程做,不需要 supplyAsync.
     *
     * @param clientId   客户端ID
     * @param password   客户端密码
     * @param username   用户名
     * @param cert       SSL证书信息
     * @param remoteAddr 客户端远程地址
     * @param channelId  通道ID
     * @return {@link CompletableFuture} 包装的认证服务器响应,失败/超时返回 null
     */
    private CompletableFuture<JSONObject> clientConnectionAuthenticationAsync(String clientId, String password, String username, String cert, String remoteAddr, String channelId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CommonConstants.CLIENT_IDENTIFIER, clientId);
        jsonObject.put(CommonConstants.PASSWORD, password);
        jsonObject.put(CommonConstants.USERNAME, username);
        jsonObject.put(CommonConstants.PROTOCOL_TYPE, CommonConstants.MQTT_PROTOCOL_TYPE);
        jsonObject.put("remoteAddr", remoteAddr);
        jsonObject.put("channelId", channelId);

        if (StringUtils.isNotBlank(cert)) {
            jsonObject.put(CommonConstants.AUTH_MODE, 1);
            jsonObject.put(CommonConstants.CLIENT_CERTIFICATE, cert);
        } else {
            jsonObject.put(CommonConstants.AUTH_MODE, 0);
        }

        return OkHttpUtil.sendPostRequestAsync(
            authConfig.getClientAuthUrl(),
            jsonObject.toJSONString(),
            null,
            JSON::parseObject
        ).thenApply(opt -> opt.orElse(null));
    }

    /**
     * 处理MQTT3的认证响应，根据认证结果返回MQTT3的认证结果。
     *
     * @param response {@link JSONObject} 认证服务器的响应
     * @return {@link MQTT3AuthResult} MQTT3认证结果
     */
    private MQTT3AuthResult handleMQTT3AuthenticationResponse(JSONObject response) {
        if (response == null) {
            return createMQTT3RejectResponse("认证服务无响应");
        }

        boolean certificationResult = response.getBooleanValue(CommonConstants.CERTIFICATION_RESULT, false);
        log.info("MQTT3认证响应 - 认证结果: {}", certificationResult);

        if (certificationResult) {
            Ok.Builder okBuilder = buildOkResponse(response);
            return MQTT3AuthResult.newBuilder().setOk(okBuilder.build()).build();
        } else {
            return createMQTT3RejectResponse("Authentication failed");
        }
    }

    /**
     * 处理MQTT5的认证响应，根据认证结果返回MQTT5的认证结果。
     *
     * @param response {@link JSONObject} 认证服务器的响应
     * @return {@link MQTT5AuthResult} MQTT5认证结果
     */
    private MQTT5AuthResult handleMQTT5AuthenticationResponse(JSONObject response) {
        if (response == null) {
            return createMQTT5RejectResponse("认证服务无响应");
        }

        boolean certificationResult = response.getBooleanValue(CommonConstants.CERTIFICATION_RESULT, false);
        log.info("MQTT5认证响应 - 认证结果: {}", certificationResult);

        if (certificationResult) {
            return parseAuthResponseForMQTT5(response);
        } else {
            return createMQTT5RejectResponse("Authentication failed");
        }
    }

    /**
     * 解析认证响应以生成MQTT3的认证结果。
     *
     * @param responseBody 认证响应的内容
     * @return {@link MQTT3AuthResult} MQTT3认证结果
     */
    private MQTT3AuthResult parseAuthResponseForMQTT3(String responseBody) {
        JSONObject responseJson = JSON.parseObject(responseBody);
        boolean certificationResult = responseJson.getBooleanValue(CommonConstants.CERTIFICATION_RESULT, false);

        if (certificationResult) {
            Ok.Builder okBuilder = buildOkResponse(responseJson);
            return MQTT3AuthResult.newBuilder().setOk(okBuilder.build()).build();
        } else {
            return createMQTT3RejectResponse("Certification result failed");
        }
    }

    /**
     * 解析认证响应以生成MQTT5的认证结果。
     *
     * @param responseBody 认证响应的内容
     * @return {@link MQTT5AuthResult} MQTT5认证结果
     */
    private MQTT5AuthResult parseAuthResponseForMQTT5(JSONObject responseBody) {
        boolean certificationResult = responseBody.getBooleanValue(CommonConstants.CERTIFICATION_RESULT, false);

        if (certificationResult) {
            Ok.Builder okBuilder = buildOkResponse(responseBody);
            // 创建 Success 构建器并填充属性
            Success.Builder successBuilder = Success.newBuilder()
                .setTenantId(okBuilder.getTenantId())
                .setUserId(okBuilder.getUserId())
                .putAllAttrs(okBuilder.getAttrsMap());

            return MQTT5AuthResult.newBuilder()
                .setSuccess(successBuilder.build())
                .build();
        } else {
            return createMQTT5RejectResponse("Certification result failed");
        }
    }

    /**
     * 构建Ok认证结果，包括tenantId、userId和自定义ACL属性。
     *
     * @param responseJson 认证服务器返回的JSON对象
     * @return {@link Ok.Builder} 包含认证信息的Ok构建器
     */
    private Ok.Builder buildOkResponse(JSONObject responseJson) {
        Optional<DeviceInfo> deviceInfo = Optional.ofNullable(responseJson.getObject(CommonConstants.DEVICE_INFO_RESULT, DeviceInfo.class));
        String tenantId = Optional.ofNullable(responseJson.getString(CommonConstants.TENANT_ID)).orElse("");
        String deviceIdentification = deviceInfo.map(DeviceInfo::getDeviceIdentification).orElse("");
        String clientId = deviceInfo.map(DeviceInfo::getClientId).orElse("");

        Map<String, String> attrsMap = new HashMap<>();
        // 认证接口返回的设备信息 （设备ID、设备名称等）将作为自定义属性附加到会话中。
        deviceInfo.ifPresent(info -> attrsMap.put(CommonConstants.DEVICE_INFO, JSON.toJSONString(info)));

        //认证接口返回ACL 控制参数（ ACL 直接嵌入在令牌中。此信息将在发布/订阅期间用于访问控制。在当前工作流中，每个会话的 ClientInfo（在成功连接后填充）仅包含有限的保留元数据。）
        Optional.ofNullable(responseJson.getJSONArray(CommonConstants.ACL_RULE_LIST_RESULT))
            .filter(array -> !array.isEmpty())
            .map(array -> array.toJavaList(DeviceAclRule.class))
            .filter(list -> !list.isEmpty())
            .ifPresent(rules -> attrsMap.put(CommonConstants.ACL_RULE, JSON.toJSONString(rules)));

        log.info("Authentication successful - clientId: {}, tenantId: {}, attrsMap: {}", clientId, tenantId, attrsMap);

        return Ok.newBuilder()
            .setTenantId(tenantId)
            .setUserId(deviceIdentification)
            .putAllAttrs(attrsMap);
    }

    /**
     * 创建MQTT3的拒绝认证响应。
     *
     * @param reason 拒绝原因
     * @return {@link MQTT3AuthResult} 拒绝认证结果
     */
    private MQTT3AuthResult createMQTT3RejectResponse(String reason) {
        log.info("MQTT3 Authentication rejected - reason: {}", reason);
        return MQTT3AuthResult.newBuilder()
            .setReject(Reject.newBuilder()
                .setCode(Reject.Code.NotAuthorized)
                .setReason(reason)
                .build())
            .build();
    }

    /**
     * 创建MQTT5的拒绝认证响应。
     *
     * @param reason 拒绝原因
     * @return {@link MQTT5AuthResult} 拒绝认证结果
     */
    private MQTT5AuthResult createMQTT5RejectResponse(String reason) {
        log.info("MQTT5 Authentication rejected - reason: {}", reason);
        return MQTT5AuthResult.newBuilder()
            .setFailed(Failed.newBuilder()
                .setCode(Failed.Code.NotAuthorized)
                .setReason(reason)
                .build())
            .build();
    }

    /**
     * 执行 MQTT5 扩展认证。根据输入的认证数据决定是成功、继续或失败。
     * TODO 暂时先不启用
     *
     * @param authData 包含认证数据的 {@link MQTT5ExtendedAuthData} 对象
     * @return {@link CompletableFuture<MQTT5ExtendedAuthResult>} 包含认证结果
     */
//    @Override
    public CompletableFuture<MQTT5ExtendedAuthResult> extendedAuth(MQTT5ExtendedAuthData authData) {
        // 异步处理认证请求
        return CompletableFuture.supplyAsync(() -> {
            // 检查认证数据类型
            switch (authData.getTypeCase()) {
                case INITIAL -> {
                    // 处理初始认证请求
                    return processInitialAuth(authData.getInitial());
                }
                case AUTH -> {
                    // 处理继续认证请求
                    return processContinueAuth(authData.getAuth());
                }
                default -> {
                    log.error("Received unknown auth data type: {}", authData.getTypeCase());
                    return createMQTT5ExtendedRejectResponse("Unsupported auth data type");
                }
            }
        });
    }

    /**
     * 处理初始认证请求，返回继续认证或拒绝认证的结果。
     *
     * @param initialData 初始认证数据
     * @return {@link MQTT5ExtendedAuthResult} 包含继续认证或拒绝的认证结果
     */
    private MQTT5ExtendedAuthResult processInitialAuth(MQTT5ExtendedAuthData.Initial initialData) {
        log.info("Processing initial auth with data: {}", initialData);

        // 验证初始认证数据并决定是否继续认证
        if (isValidInitialData(initialData)) {
            // 构建继续认证的响应
            return MQTT5ExtendedAuthResult.newBuilder()
                .setSuccess(Success.newBuilder()
                    .setTenantId("thinglinks")
                    .setUserId("User456")
                    .putAttrs("role", "user")
                    .build())
                .build();
        } else {
            // 认证失败，返回拒绝响应
            return createMQTT5ExtendedRejectResponse("Initial authentication failed");
        }
    }

    private boolean isValidInitialData(MQTT5ExtendedAuthData.Initial initialData) {
        // 验证初始认证数据的逻辑
        return true;
    }

    private boolean isValidAuthData(MQTT5ExtendedAuthData.Auth authData) {
        // 验证继续认证数据的逻辑
        return true;
    }

    private MQTT5ExtendedAuthResult processContinueAuth(MQTT5ExtendedAuthData.Auth authData) {
        log.info("Processing continue auth with data: {}", authData);

        // 根据继续认证数据进行实际的认证操作
        if (isValidAuthData(authData)) {
            return MQTT5ExtendedAuthResult.newBuilder()
                .setSuccess(Success.newBuilder()
                    .setTenantId("thinglinks")
                    .setUserId("User456")
                    .putAttrs("role", "user")
                    .build())
                .build();
        } else {
            return createMQTT5ExtendedRejectResponse("Continue authentication failed");
        }
    }

    private MQTT5ExtendedAuthResult createMQTT5ExtendedRejectResponse(String reason) {
        log.info("MQTT5 Extended Auth rejected - reason: {}", reason);
        return MQTT5ExtendedAuthResult.newBuilder()
            .setFailed(Failed.newBuilder()
                .setCode(Failed.Code.NotAuthorized)
                .setReason(reason)
                .build())
            .build();
    }

    /**
     * 执行客户端ACL权限检查
     * <p>★ 用 {@link AsyncCache#get(Object, java.util.function.BiFunction)} 替代 getIfPresent+put 两步:
     * <ul>
     *   <li>并发同 key miss 时 Caffeine 内部 dedup,只跑一次 performAclCheck</li>
     *   <li>loader 异常完成时自动不缓存,后续请求重试</li>
     *   <li>无 race condition,无需手动 whenComplete put</li>
     * </ul>
     *
     * @param client 客户端信息，包含认证元数据
     * @param action 客户端操作（PUB/SUB/UNSUB）
     * @return 检查结果（true表示允许，false表示拒绝）
     */
    @Override
    public CompletableFuture<Boolean> check(ClientInfo client, MQTTAction action) {
        // 如果配置文件中 ACL功能 未启用，直接放行
        if (!aclConfig.isEnabled()) {
            return CompletableFuture.completedFuture(true);
        }
        // 如果配置文件中 租户白名单 中包含当前租户ID，直接放行
        if (aclConfig.getTenantWhitelist().contains(client.getTenantId())) {
            return CompletableFuture.completedFuture(true);
        }

        CacheKey cacheKey = buildAclCacheKey(client, action);
        // AsyncCache.get(): 命中返回 in-memory CompletableFuture(μs);
        // 未命中调 loader,期间并发请求共享同一 future(防击穿).
        return aclCache.get(cacheKey, (k, exec) -> performAclCheck(client, action))
            .exceptionally(e -> {
                log.warn("ACL check failed for tenantId:{}", client.getTenantId(), e);
                return false;
            });
    }

    /**
     * 执行ACL权限检查的核心方法
     * 处理两种ACL检查场景：
     * 1. 优先检查客户端元数据中的ACL规则（快速路径,CPU 在 executor 上做避免阻塞 BifroMQ event loop）
     * 2. 如果元数据中没有有效规则,异步调用 API 接口检查（回退路径）
     *
     * @param client 客户端信息，包含认证元数据
     * @param action 客户端操作（PUB/SUB/UNSUB）
     * @return {@link CompletableFuture<Boolean>} 异步返回鉴权结果
     */
    private CompletableFuture<Boolean> performAclCheck(ClientInfo client, MQTTAction action) {
        // 步骤1-2: 构建请求 + 元数据匹配(CPU 工作放 executor)
        return CompletableFuture.supplyAsync(() -> {
            JSONObject aclRequest = buildAclRequest(client, action);
            Optional<Boolean> metadataCheckResult = checkAclFromClientMetadata(client, aclRequest);
            // 用 2-tuple 透传给下一阶段;数组比新增类轻量
            return new Object[]{aclRequest, metadataCheckResult};
        }, executor).thenCompose(arr -> {
            @SuppressWarnings("unchecked")
            Optional<Boolean> cached = (Optional<Boolean>) arr[1];
            if (cached.isPresent()) {
                return CompletableFuture.completedFuture(cached.get());
            }
            // 步骤3: 元数据未命中,异步走 HTTP API
            return checkAclViaHttpApiAsync((JSONObject) arr[0]);
        });
    }

    /**
     * 从客户端元数据中检查ACL权限
     *
     * @param client     包含认证元数据的客户端信息
     * @param aclRequest 已构建的ACL请求参数
     * @return Optional<Boolean>
     * - 包含true/false表示元数据中有有效规则时的鉴权结果
     * - empty表示元数据中没有有效规则需要走API检查
     */
    private Optional<Boolean> checkAclFromClientMetadata(ClientInfo client, JSONObject aclRequest) {
        try {
            // 步骤1：从元数据中提取ACL规则字符串
            return Optional.ofNullable(client.getMetadataMap().get(CommonConstants.ACL_RULE))
                // 过滤空值
                .filter(StrUtil::isNotBlank)
                // 转换为ACL规则对象列表
                .map(acl -> JSONArray.parseArray(acl, DeviceAclRule.class))
                // 过滤空规则列表
                .filter(rules -> !rules.isEmpty())
                // 执行规则匹配
                .flatMap(rules -> {
                    // 步骤2：解析动作类型
                    Optional<ClientAclActionTypeEnum> actionType = Optional.ofNullable(aclRequest.getInteger(CommonConstants.ACTION_TYPE))
                        .flatMap(ClientAclActionTypeEnum::fromValue);

                    // 步骤3：转换为规则动作类型
                    Optional<DeviceAclRuleActionTypeEnum> ruleActionType = actionType
                        .flatMap(DeviceAclRuleActionTypeEnum::fromClientType);

                    // 无效动作类型直接返回空
                    if (ruleActionType.isEmpty()) {
                        return Optional.empty();
                    }

                    // 步骤4：过滤出适用的规则
                    List<DeviceAclRule> filteredRules = rules.stream()
                        .filter(DeviceAclRule::getEnabled)
                        .filter(rule ->
                            rule.getActionType().equals(ruleActionType.get().getValue()) ||
                                rule.getActionType().equals(DeviceAclRuleActionTypeEnum.ALL.getValue())
                        )
                        .collect(Collectors.toList());

                    // 步骤5：如果没有适用的规则，返回empty回退到API检查
                    if (filteredRules.isEmpty()) {
                        return Optional.empty();
                    }

                    // 步骤6：执行主题匹配
                    Optional<DeviceInfo> deviceInfoOptional = Optional.ofNullable(client.getMetadataMap().get(CommonConstants.DEVICE_INFO))
                        .filter(StrUtil::isNotBlank)
                        .map(deviceInfo -> JSON.parseObject(deviceInfo, DeviceInfo.class));
                    AclTopicPatternPlaceholderReplacer.replacePlaceholders(filteredRules, deviceInfoOptional);
                    return Optional.of(isTopicAllowedByRules(
                        aclRequest.getString(CommonConstants.TOPIC), filteredRules));
                });
        } catch (Exception e) {
            log.error("从客户端元数据中检查ACL权限失败（回退至API检查） - 错误信息: {}", e.getMessage());
            return Optional.empty();
        }

    }

    /**
     * 异步通过HTTP API检查ACL权限（回退路径,生产推荐）
     * <p>★ 使用 {@link OkHttpUtil#sendPostRequestForStatusAsync},不阻塞调用线程.
     *
     * @param aclRequest 完整的ACL检查请求参数
     * @return {@link CompletableFuture<Boolean>}
     * - true: 允许访问(HTTP 200)
     * - false: 拒绝访问或检查失败(任意非 200 / 网络异常)
     */
    private CompletableFuture<Boolean> checkAclViaHttpApiAsync(JSONObject aclRequest) {
        return OkHttpUtil.sendPostRequestForStatusAsync(
            aclConfig.getAclCheckUrl(),
            aclRequest.toJSONString(),
            null
        ).thenApply(statusCode -> {
            if (log.isDebugEnabled()) {
                log.debug("ACL API 异步检查完成 - 状态码: {}, 请求参数: {}", statusCode, aclRequest);
            }
            return statusCode == HttpStatus.OK.value();
        });
    }

    /**
     * 同步通过HTTP API检查ACL权限(保留,不再被 performAclCheck 调用,
     * 仅供需要同步语义的场景使用,新代码推荐 {@link #checkAclViaHttpApiAsync}).
     *
     * @param aclRequest 完整的ACL检查请求参数
     * @return boolean
     * - true: 允许访问
     * - false: 拒绝访问或检查失败
     */
    private boolean checkAclViaHttpApi(JSONObject aclRequest) {
        try {
            int statusCode = OkHttpUtil.sendPostRequestForStatus(
                aclConfig.getAclCheckUrl(),
                aclRequest.toJSONString(),
                null
            );
            log.debug("ACL API检查完成 - 状态码: {}, 请求参数: {}", statusCode, aclRequest);
            return statusCode == HttpStatus.OK.value();
        } catch (Exception e) {
            log.error("ACL API检查失败 - 请求参数: {}, 错误信息: {}", aclRequest, e.getMessage());
            return false;
        }
    }

    /**
     * 构建ACL检查请求参数
     *
     * @param client 客户端信息
     * @param action 客户端操作
     * @return 包含所有请求参数的JSON对象
     */
    private JSONObject buildAclRequest(ClientInfo client, MQTTAction action) {
        JSONObject aclRequest = new JSONObject();

        // 添加基础信息
        aclRequest.put(CommonConstants.TENANT_ID, client.getTenantId());
        aclRequest.put(CommonConstants.PROTOCOL_TYPE, client.getType());

        // 安全处理 metadataMap
        Optional.of(client.getMetadataMap())
            .ifPresent(metadataMap -> {
                aclRequest.put(CommonConstants.CLIENT_IDENTIFIER, metadataMap.getOrDefault(CommonConstants.CLIENT_ID, ""));
                aclRequest.put(CommonConstants.USER_ID, metadataMap.getOrDefault(CommonConstants.USER_ID, ""));
                aclRequest.put("channelId", metadataMap.getOrDefault("channelId", ""));
                aclRequest.put("broker", metadataMap.getOrDefault("broker", ""));
                aclRequest.put("remoteAddr", metadataMap.getOrDefault("address", ""));
            });

        // 添加操作相关参数
        aclRequest.put(CommonConstants.ACTION_TYPE, resolveActionType(action));
        aclRequest.put(CommonConstants.TOPIC, resolveActionTopic(action));

        return aclRequest;
    }

    /**
     * 解析操作类型
     *
     * @param action 客户端操作
     * @return 对应的操作类型枚举值（可能为null）
     */
    private Integer resolveActionType(MQTTAction action) {
        return Optional.of(action)
            .map(a -> {
                if (a.hasPub()) {
                    return ClientAclActionTypeEnum.PUBLISH.getValue();
                }
                if (a.hasSub()) {
                    return ClientAclActionTypeEnum.SUBSCRIBE.getValue();
                }
                if (a.hasUnsub()) {
                    return ClientAclActionTypeEnum.UNSUBSCRIBE.getValue();
                }
                return ClientAclActionTypeEnum.UNKNOWN.getValue();
            })
            .orElse(null);
    }

    /**
     * 解析操作主题
     *
     * @param action 客户端操作
     * @return 对应的主题字符串（空字符串表示无主题）
     */
    private String resolveActionTopic(MQTTAction action) {
        return Optional.of(action)
            .map(a -> {
                if (a.hasPub()) {
                    return a.getPub().getTopic();
                }
                if (a.hasSub()) {
                    return a.getSub().getTopicFilter();
                }
                if (a.hasUnsub()) {
                    return a.getUnsub().getTopicFilter();
                }
                return "";
            })
            .orElse("");
    }

    private CacheKey buildAclCacheKey(ClientInfo client, MQTTAction action) {
        String clientId = client.getMetadataMap().get("clientId");
        String topic = resolveActionTopic(action);
        // ★ 用预编译 Pattern.matcher().replaceAll(),避免 String.replaceAll 每次重新编译正则
        String normalizedTopic = MULTI_SLASH_PATTERN.matcher(topic).replaceAll("/");

        // 使用组合键：clientId + actionType + normalizedTopic
        String cacheKeyStr = String.format("%s|%s|%s", clientId, action.getTypeCase().name(), normalizedTopic);
        return new CacheKey(cacheKeyStr);
    }

    /**
     * 失效指定 clientId 的所有 ACL 缓存条目.
     * <p>当前是 O(n) 全表扫描;若 clientId 失效频率高且 cache 容量大(1M+),
     * 后续可考虑维护 clientId → CacheKey 集合的二级索引(此处暂保留 scan 简单实现).
     * <p>{@code aclCache.synchronous()} 返回 {@link com.github.benmanes.caffeine.cache.Cache} 视图,
     * 因为 AsyncCache.asMap() 返回 ConcurrentMap<K, CompletableFuture<V>>,直接迭代 future 没意义.
     */
    public void invalidateAclCache(String clientId) {
        if (aclCache == null) {
            return;
        }
        String prefix = clientId + "|";
        long count = aclCache.synchronous().asMap().keySet().stream()
            .filter(key -> key.getKey().startsWith(prefix))
            .peek(aclCache.synchronous()::invalidate)
            .count();
        log.debug("已失效clientId:[{}]...{}条Acl缓存", clientId, count);
    }


    @Override
    public void close() {
        if (stopped.compareAndSet(false, true)) {
            log.info("Closing auth provider manager");
        }
    }

}
