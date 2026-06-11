package com.mqttsnet.thinglinks.bridge.controller.anyuser;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.databridge.model.SourceMessage;
import com.mqttsnet.basic.databridge.source.http.HttpSource;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.entity.bridge.DataSource;
import com.mqttsnet.thinglinks.entity.bridge.SubscriptionSource;
import com.mqttsnet.thinglinks.service.bridge.DataSourceService;
import com.mqttsnet.thinglinks.service.bridge.SubscriptionSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * HTTP 入站桥接 endpoint(第三方主动 POST 数据进来)。走 /anyUser/ 前缀免登录开放接口,POST /anyUser/bridge/ingress/{sourceCode},通过 header X-Signature = HMAC_SHA256(timestamp + body).hex() + X-Timestamp(epoch millis) 签名防重放。
 * 链路:校验 → HttpSource.ingest → LifecycleManager 注册的 handler 字段映射 → RocketMQ thinglinks-bridge-ingress → mqs BridgeIngressConsumer 按 targetHandler 分发。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Tag(name = "桥接入站 HTTP endpoint")
@Slf4j
@RestController
@RequestMapping("/anyUser/bridge/ingress")
@RequiredArgsConstructor
public class BridgeIngressOpenAnyUserController {

    /**
     * 时间戳偏差防重放窗口(5 分钟)。
     */
    private static final long TIMESTAMP_TOLERANCE_MS = 5 * 60 * 1000L;

    /**
     * 收集 headers 时过滤的敏感字段。
     */
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie");

    private final HttpSource httpSource;
    private final SubscriptionSourceService subscriptionSourceService;
    private final DataSourceService dataSourceService;

    @Operation(summary = "桥接入站 endpoint")
    @PostMapping("/{sourceCode}")
    public R<Void> ingress(
            @PathVariable("sourceCode") String sourceCode,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestBody(required = false) String body,
            HttpServletRequest request) {
        // 1) 查订阅源(走 Service,@DS 切租户库)
        SubscriptionSource src = Optional.ofNullable(subscriptionSourceService.getByCode(sourceCode))
                .orElseThrow(() -> BizException.wrap("订阅源不存在 sourceCode=" + sourceCode));
        if (!Boolean.TRUE.equals(src.getEnable())) {
            throw BizException.wrap("订阅源未启用 sourceCode=" + sourceCode);
        }

        // 2) 校验签名(credentialJson 配置 secretKey 时启用)
        DataSource ds = Optional.ofNullable(dataSourceService.getById(src.getDataSourceId()))
                .orElseThrow(() -> BizException.wrap("关联数据源不存在 dsId=" + src.getDataSourceId()));
        verifySignatureIfRequired(ds, signature, timestamp, body);

        // 3) 构造 SourceMessage 触发 handler
        Map<String, String> headers = sanitizeHeaders(collectHeaders(request));
        SourceMessage msg = SourceMessage.builder()
                .body(body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8))
                .headers(headers)
                .routingKey(sourceCode)
                .ts(System.currentTimeMillis())
                .sourceMessageId(MapUtil.getStr(headers, "X-Request-Id"))
                .build();
        if (!httpSource.ingest(sourceCode, msg)) {
            log.warn("[BridgeIngress] no handler registered for sourceCode={} (lifecycle not started?)",
                    sourceCode);
            throw BizException.wrap("订阅源回调未注册,请检查订阅源是否启用并使用 HTTP 协议数据源");
        }
        return R.success(null);
    }

    /**
     * 校验签名(credentialJson 含 secretKey 时启用)。
     * 时间戳必须在窗口内 + signature = HMAC_SHA256(timestamp + body, secretKey).hex()。
     *
     * @param ds 关联数据源(含 credentialJson)
     * @param signature 请求头 X-Signature
     * @param timestamp 请求头 X-Timestamp(epoch millis)
     * @param body 请求体
     */
    private void verifySignatureIfRequired(DataSource ds, String signature, String timestamp, String body) {
        String secretKey = extractSecretKey(ds.getCredentialJson());
        if (StrUtil.isBlank(secretKey)) {
            // 未配 secretKey → 跳过签名校验(依靠网络层 / API 网关鉴权)
            return;
        }
        if (StrUtil.hasBlank(signature, timestamp)) {
            throw BizException.wrap("缺少 X-Signature / X-Timestamp header");
        }
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw BizException.wrap("X-Timestamp 格式无效");
        }
        if (Math.abs(System.currentTimeMillis() - ts) > TIMESTAMP_TOLERANCE_MS) {
            throw BizException.wrap("请求时间戳超出窗口(防重放)");
        }
        HMac hmac = SecureUtil.hmacSha256(secretKey.getBytes(StandardCharsets.UTF_8));
        String expected = hmac.digestHex(timestamp + StrUtil.nullToEmpty(body));
        if (!StrUtil.equalsIgnoreCase(expected, signature)) {
            log.warn("[BridgeIngress] signature mismatch expected_len={} actual_len={}",
                    expected.length(), signature.length());
            throw BizException.wrap("签名校验失败");
        }
    }

    /**
     * 从 credentialJson 解出 secretKey;不合法返 null 跳过签名校验。
     *
     * @param credentialJson 数据源凭证 JSON
     * @return secretKey;JSON 不合法或缺失返 null
     */
    private String extractSecretKey(String credentialJson) {
        if (!JSON.isValidObject(credentialJson)) {
            return null;
        }
        return JSON.parseObject(credentialJson).getString("secretKey");
    }

    /**
     * 收集所有请求头。
     *
     * @param request HTTP 请求
     * @return 请求头名值映射
     */
    private Map<String, String> collectHeaders(HttpServletRequest request) {
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return MapUtil.empty();
        }
        Map<String, String> out = MapUtil.newHashMap();
        Collections.list(names).forEach(name -> {
            if (StrUtil.isNotBlank(name)) {
                out.put(name, request.getHeader(name));
            }
        });
        return out;
    }

    /**
     * 过滤敏感 headers(authorization / cookie)。
     *
     * @param raw 原始请求头映射
     * @return 过滤敏感字段后的请求头映射
     */
    private Map<String, String> sanitizeHeaders(Map<String, String> raw) {
        if (MapUtil.isEmpty(raw)) {
            return MapUtil.empty();
        }
        Map<String, String> out = MapUtil.newHashMap(raw.size());
        raw.forEach((name, value) -> {
            if (!isSensitive(name)) {
                out.put(name, value);
            }
        });
        return out;
    }

    private boolean isSensitive(String headerName) {
        return SENSITIVE_HEADERS.stream().anyMatch(s -> s.equalsIgnoreCase(headerName));
    }
}
