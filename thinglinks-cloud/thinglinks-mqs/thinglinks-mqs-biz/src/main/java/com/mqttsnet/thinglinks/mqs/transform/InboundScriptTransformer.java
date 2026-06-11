package com.mqttsnet.thinglinks.mqs.transform;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.basic.utils.topic.MqttTopicMatcher;
import com.mqttsnet.basic.utils.topic.TopicPlaceholders;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.common.cache.rule.groovy.GroovyScriptCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.rule.groovy.TransformScriptEntry;
import com.mqttsnet.thinglinks.common.constant.CommonIotConstants;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.entity.uplink.source.UplinkMessageEventSource;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import com.mqttsnet.thinglinks.rule.facade.RuleOpenAnyUserFacade;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptDirectCompileParam;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备上行原始报文「前置转换」── 在 topic 路由({@code TopicHandlerFactory})之前执行:
 * 按「产品 + 产品发布版本 + topic 模式」命中用户配置的 Groovy 脚本,把厂商私有 topic/报文
 * 转换成平台标准 {@code /v1/devices/{deviceId}/datas} 报文,再交后续 handler 走原有链路。
 *
 * <p>通用于所有上行 topic(不止 datas):未命中绑定脚本则原样透传,命中才转换;
 * 任何异常一律降级为原样透传,绝不阻断上行主链路。
 *
 * <p>缓存:{@code HGETALL(产品+版本桶)} 一次取回该产品该版本全部 {@code topic 模式 → 脚本内容},
 * 内存逐条匹配;无绑定时桶为空,零额外开销(不发 Feign)。
 *
 * <p>脚本 I/O 契约:
 * <ul>
 *   <li>入参(executeParams JSON):{@code originTopic / originBody / clientId / deviceIdentification / productIdentification};
 *       另注入 {@code device}(设备基础信息 DTO,不含 password)与 {@code product}(产品基础信息 DTO),
 *       脚本可 {@code device.signKey} / {@code device.encryptMethod} / {@code product.protocolType} 动态取值</li>
 *   <li>出参(脚本 return,即 context):{@code {"topic":"/v1/devices/.../datas","payload":{平台标准结构}}};
 *       缺 {@code topic} 保持原 topic;context 非约定结构则整体当作 payload。</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-06-03
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InboundScriptTransformer {

    private static final String CTX_TOPIC = CommonIotConstants.TOPIC;
    private static final String CTX_PAYLOAD = CommonIotConstants.PAYLOAD;
    private static final String CHANNEL_MQTT = "mqtt";
    private static final String CHANNEL_WEBSOCKET = "webSocket";

    private final CachePlusOps cachePlusOps;
    private final LinkCacheDataHelper linkCacheDataHelper;
    private final RuleOpenAnyUserFacade ruleOpenAnyUserFacade;
    private final ScriptBindingAssembler bindingAssembler;

    /**
     * 解析出可路由的 {@link UplinkMessageEventSource} ── 命中前置转换脚本则返回转换后的
     * (改写 topic + 标准报文),否则按原始事件构建。
     *
     * @param event 设备通用事件(PUBLISH)
     * @return 可交 {@code TopicHandlerFactory} 路由的消息源
     */
    public UplinkMessageEventSource resolveEventSource(CommonDeviceEvent event) {
        // 优先用 bus 阶段已解析透传的设备缓存(免重取);缺失(如未经 bus 富化的边缘场景)兜底自取。
        // 解析一次透传到 source(下游 handler 免重取)+ 供脚本命中判断复用。
        DeviceCacheVO deviceVO = Optional.ofNullable(event.getDeviceCache()).orElseGet(() -> resolveDevice(event));
        try {
            Transformed t = tryTransform(event, deviceVO);
            if (t != null) {
                log.info("[InboundTransform] applied clientId={} topic {} -> {}", event.getClientId(), event.getTopic(), t.topic());
                return buildTransformedSource(event, t.topic(), t.payload(), deviceVO);
            }
        } catch (Exception e) {
            log.warn("[InboundTransform] failed (passthrough) clientId={} topic={} err={}",
                event.getClientId(), event.getTopic(), e.getMessage());
        }
        return buildOriginalSource(event, deviceVO);
    }

    /**
     * 命中并执行脚本则返回转换结果,否则返回 {@code null}(走原样透传)。
     */
    private Transformed tryTransform(CommonDeviceEvent event, DeviceCacheVO deviceVO) {
        String topic = event.getTopic();
        String product = event.getProductIdentification();
        if (StrUtil.hasBlank(topic, product)) {
            return null;
        }
        String channel = resolveChannel(event.getProtocolType());
        if (channel == null) {
            return null;
        }
        String version = Optional.ofNullable(deviceVO).map(DeviceCacheVO::getBoundProductVersionNo).orElse(null);
        if (StrUtil.isBlank(version)) {
            return null;
        }
        CacheKey bucketKey = GroovyScriptCacheKeyBuilder.transformHashKey(channel, product, version);
        Map<String, CacheResult<String>> bucket =
            cachePlusOps.hGetAll(bucketKey, k -> Collections.<String, String>emptyMap(), false);
        if (CollUtil.isEmpty(bucket)) {
            return null;
        }
        MatchedScript matched = matchScript(bucket, topic);
        if (matched == null || StrUtil.isBlank(matched.entry().getContent())) {
            return null;
        }
        // 命中后才取产品缓存(未命中不查);设备缓存上面已取,直接复用注入脚本
        ProductCacheVO productVO = linkCacheDataHelper.getProductCacheVO(product).orElse(null);
        // 脚本唯一键(与详情 RuleGroovyScriptResultVO#buildOnlyKey 一致):脚本类型:渠道:产品:主题模式 ── 供 rule 记执行统计
        String scriptUniqueKey = String.join(StrPool.COLON, GroovyScriptCacheKeyBuilder.TRANSFORM_SCRIPT_TYPE, channel, product, matched.topicPattern());
        return executeScript(event, topic, matched.entry(), scriptUniqueKey, deviceVO, productVO);
    }

    /**
     * 桶内按 topic 模式逐条匹配,命中返该条 {@link TransformScriptEntry}(脚本内容 + 扩展参数)。
     *
     * <p>与桥接 {@code TopicMatchStrategy} / ACL 鉴权同一套语义:先用
     * {@link TopicPlaceholders#replaceWithWildcard} 把 {@code ${...}} 占位符转成 MQTT 单层通配 {@code +},
     * 再交 {@link MqttTopicMatcher} 与真实上行 topic 比对 ── 直接拿 {@code ${...}} 原串匹配会被当字面量,永远不命中。
     */
    private MatchedScript matchScript(Map<String, CacheResult<String>> bucket, String topic) {
        for (Map.Entry<String, CacheResult<String>> entry : bucket.entrySet()) {
            String mqttPattern = TopicPlaceholders.replaceWithWildcard(entry.getKey());
            if (MqttTopicMatcher.match(mqttPattern, topic)) {
                TransformScriptEntry scriptEntry = parseBucketValue(entry.getValue() == null ? null : entry.getValue().getRawValue());
                // entry.getKey() 即命中的主题模式(topicPattern,桶 field),回传用于拼脚本唯一键
                return scriptEntry == null ? null : new MatchedScript(entry.getKey(), scriptEntry);
            }
        }
        return null;
    }

    /**
     * 命中的脚本 ── 匹配到的主题模式(topicPattern,桶 field) + 脚本条目。
     */
    private record MatchedScript(String topicPattern, TransformScriptEntry entry) {
    }

    /**
     * 解析桶值 JSON → {@link TransformScriptEntry}。兼容旧格式(纯脚本内容字符串):非 JSON 则整串当 content。
     */
    private TransformScriptEntry parseBucketValue(String raw) {
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        try {
            TransformScriptEntry entry = JSON.parseObject(raw, TransformScriptEntry.class);
            if (entry != null && StrUtil.isNotBlank(entry.getContent())) {
                return entry;
            }
        } catch (Exception ignore) {
            // 旧格式(纯脚本内容)走下面兜底
        }
        return new TransformScriptEntry(raw, null);
    }

    /**
     * Feign 调 rule 按脚本内容执行,解析 context → {改写 topic, 标准报文 payload}。
     * 注入 {@code device}(独立 DTO,不含 password)+ {@code product}(独立 DTO)+ {@code config}(脚本 extend_params),
     * 脚本可 {@code device.signKey} / {@code device.encryptMethod} / {@code product.protocolType} / {@code config.xxx} 动态取值。
     */
    private Transformed executeScript(CommonDeviceEvent event, String topic, TransformScriptEntry entry,
                                      String scriptUniqueKey, DeviceCacheVO deviceVO, ProductCacheVO productVO) {
        // 运行时:按设备绑定版本(回退产品生效版本)解析物模型,注入脚本(与在线调试同款绑定)
        String modelVersionNo = deviceVO == null ? null : deviceVO.getBoundProductVersionNo();
        if (StrUtil.isBlank(modelVersionNo) && productVO != null) {
            modelVersionNo = productVO.getActiveVersionNo();
        }
        ProductModelCacheVO productModel = StrUtil.isBlank(modelVersionNo) ? null
            : linkCacheDataHelper.resolveProductModelByVersionNo(event.getProductIdentification(), modelVersionNo).orElse(null);

        // 与「在线调试」共用同一套绑定组装(零漂移):originTopic / originBody / clientId / device / product / productModel / config …
        Map<String, Object> params = bindingAssembler.assemble(deviceVO, productVO, productModel,
            topic, decodeBody(event), event.getPayloadHex(),
            event.getClientId(), event.getDeviceIdentification(), event.getProductIdentification(),
            entry.getExtendParams());

        RuleGroovyScriptDirectCompileParam param = new RuleGroovyScriptDirectCompileParam();
        param.setScriptContent(entry.getContent());
        param.setExecuteParams(JSON.toJSONString(params));
        // 脚本唯一键透传给 rule,供按脚本维度记执行统计(total/success/fail)
        param.setScriptUniqueKey(scriptUniqueKey);

        R<GroovyScriptEngineExecutorResultVO> r = ruleOpenAnyUserFacade.executeScriptContent(param);
        if (r == null || !Boolean.TRUE.equals(r.getIsSuccess()) || r.getData() == null) {
            log.warn("[InboundTransform] script exec non-success clientId={} topic={} r={}", event.getClientId(), topic, JSON.toJSONString(r));
            return null;
        }
        Object context = r.getData().getContext();
        if (context == null) {
            return null;
        }
        JSONObject ctx = toJsonObject(context);
        String newTopic = ctx == null ? null : ctx.getString(CTX_TOPIC);
        String payload;
        if (ctx != null && ctx.containsKey(CTX_PAYLOAD)) {
            Object payloadVal = ctx.get(CTX_PAYLOAD);
            payload = payloadVal instanceof String ? (String) payloadVal : JSON.toJSONString(payloadVal);
        } else {
            // 非约定结构:整体当作平台标准报文
            payload = context instanceof String ? (String) context : JSON.toJSONString(context);
        }
        if (StrUtil.isBlank(payload)) {
            return null;
        }
        return new Transformed(StrUtil.isBlank(newTopic) ? topic : newTopic, payload);
    }

    private static JSONObject toJsonObject(Object context) {
        try {
            if (context instanceof JSONObject) {
                return (JSONObject) context;
            }
            if (context instanceof Map) {
                return JSON.parseObject(JSON.toJSONString(context));
            }
            if (context instanceof String) {
                return JSON.parseObject((String) context);
            }
        } catch (Exception ignore) {
            // 解析失败按 null 处理,交由调用方兜底当作整体 payload
        }
        return null;
    }

    /**
     * 取设备缓存 VO ── 既用于解析绑定的产品发布版本(版本维度定位桶),也作为脚本 {@code device} 绑定的来源。
     */
    private DeviceCacheVO resolveDevice(CommonDeviceEvent event) {
        String key = StrUtil.blankToDefault(event.getDeviceIdentification(), event.getClientId());
        if (StrUtil.isBlank(key)) {
            return null;
        }
        return linkCacheDataHelper.getDeviceCacheVO(key).orElse(null);
    }

    /**
     * 协议类型 → 渠道编码(与脚本 channelCode 字典对齐:mqtt / webSocket)。
     */
    private String resolveChannel(String protocolType) {
        if (StrUtil.isBlank(protocolType)) {
            return null;
        }
        if (ProtocolTypeEnum.MQTT.getValue().equalsIgnoreCase(protocolType)) {
            return CHANNEL_MQTT;
        }
        if (ProtocolTypeEnum.WEBSOCKET.getValue().equalsIgnoreCase(protocolType)) {
            return CHANNEL_WEBSOCKET;
        }
        return null;
    }

    private String decodeBody(CommonDeviceEvent event) {
        if (StrUtil.isBlank(event.getPayload())) {
            return StrUtil.EMPTY;
        }
        return new String(Base64.decode(event.getPayload()), StandardCharsets.UTF_8);
    }

    /**
     * 按转换后的 topic + 标准报文构建消息源(payload 重新 Base64,供后续 handler 解码)。
     */
    private UplinkMessageEventSource buildTransformedSource(CommonDeviceEvent event, String topic, String payload, DeviceCacheVO deviceVO) {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        return UplinkMessageEventSource.builder()
            .topic(topic)
            .qos(event.getQos() == null ? null : String.valueOf(event.getQos()))
            .payloadBytes(bytes)
            .payload(Base64.encode(bytes))
            .encoding(StandardCharsets.UTF_8.name())
            .originalSize(String.valueOf(bytes.length))
            .timestamp(event.getTs() == null ? null : String.valueOf(event.getTs()))
            .deviceCacheVO(deviceVO)
            .build();
    }

    /**
     * 原样透传:按原始事件构建消息源(与未启用前置转换时行为一致)。
     */
    private UplinkMessageEventSource buildOriginalSource(CommonDeviceEvent event, DeviceCacheVO deviceVO) {
        byte[] payloadBytes = StrUtil.isBlank(event.getPayload())
            ? new byte[0]
            : Base64.decode(event.getPayload());
        return UplinkMessageEventSource.builder()
            .topic(event.getTopic())
            .qos(event.getQos() == null ? null : String.valueOf(event.getQos()))
            .payloadBytes(payloadBytes)
            .payload(event.getPayload())
            .payloadHex(event.getPayloadHex())
            .originalSize(event.getOriginalSize() == null ? null : String.valueOf(event.getOriginalSize()))
            .encoding(event.getEncoding())
            .timestamp(event.getTs() == null ? null : String.valueOf(event.getTs()))
            .deviceCacheVO(deviceVO)
            .build();
    }

    /**
     * 转换结果 ── 改写后的 topic + 平台标准报文 payload。
     */
    private record Transformed(String topic, String payload) {
    }
}
