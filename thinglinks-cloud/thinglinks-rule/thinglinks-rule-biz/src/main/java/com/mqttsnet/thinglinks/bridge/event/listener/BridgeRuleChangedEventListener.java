package com.mqttsnet.thinglinks.bridge.event.listener;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.groovy.helper.RegisterScriptHelper;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.bridge.event.BridgeRuleChangedEvent;
import com.mqttsnet.thinglinks.bridge.event.BridgeRuleDeletedEvent;
import com.mqttsnet.thinglinks.bridge.event.DataSourceChangedEvent;
import com.mqttsnet.thinglinks.bridge.event.DataSourceDeletedEvent;
import com.mqttsnet.thinglinks.bridge.event.RuleGroovyScriptChangedEvent;
import com.mqttsnet.thinglinks.bridge.event.RuleGroovyScriptDeletedEvent;
import com.mqttsnet.thinglinks.bridge.event.source.BridgeRuleChangedEventSource;
import com.mqttsnet.thinglinks.bridge.event.source.RuleGroovyScriptChangedEventSource;
import com.mqttsnet.thinglinks.cache.helper.RuleCacheDataHelper;
import com.mqttsnet.thinglinks.common.cache.rule.groovy.GroovyScriptCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.rule.groovy.TransformScriptEntry;
import com.mqttsnet.thinglinks.entity.script.RuleGroovyScript;
import com.mqttsnet.thinglinks.record.script.ScriptIdentifier;
import com.mqttsnet.thinglinks.service.bridge.DataBridgeService;
import com.mqttsnet.thinglinks.service.bridge.DataSourceService;
import com.mqttsnet.thinglinks.service.script.RuleGroovyScriptService;
import com.mqttsnet.thinglinks.vo.query.script.RuleGroovyScriptQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.DataBridgeResultVO;
import com.mqttsnet.thinglinks.vo.result.bridge.DataSourceResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Rule 域缓存事件监听器 ── 按事件类型直接注册
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BridgeRuleChangedEventListener {

    private final RuleCacheDataHelper ruleCacheDataHelper;
    private final DataSourceService dataSourceService;
    private final DataBridgeService dataBridgeService;
    private final RuleGroovyScriptService ruleGroovyScriptService;
    private final RegisterScriptHelper registerScriptHelper;

    // ============================== 桥接规则 ==============================

    /**
     * 规则变更事件 ── del 旧 bucket + 直查 DB(转 VO)拿当前启用规则 + 写入新 bucket。
     * <p>不走 {@code getBridgeEnabledRules} 缓存读路径而直接调
     * {@link DataBridgeService#getEnabledRules},避免事件监听里再触发 cache miss
     * 形成循环刷新。Service 层已通过 BeanPlusUtil 完成 entity → VO 转换,
     * helper 内部再完成 ResultVO → CacheVO 第二次转换并 hMSet。
     */
    @EventListener
    public void onBridgeRuleChanged(BridgeRuleChangedEvent event) {
        BridgeRuleChangedEventSource src = event.getEventSource();
        String appId = src.getAppId();
        String direction = src.getDirection();
        // del 旧 bucket
        ruleCacheDataHelper.delBridgeEnabledRulesBucket(appId, direction);
        // 直接查 DB 拿当前启用的规则 VO 列表(Service 内部已 BeanPlusUtil 转 VO,helper 不再持有 entity)
        List<DataBridgeResultVO> rules = dataBridgeService.getEnabledRules(appId, direction);
        // helper 内部把 ResultVO 再转 CacheVO 写入 bucket
        ruleCacheDataHelper.setBridgeEnabledRulesBucket(appId, direction, rules);
        log.info("[BridgeRuleChangedEventListener] onBridgeRuleChanged ruleId={} appId={} direction={} → refresh bucket size={}", src.getRuleId(), appId, direction, rules == null ? 0 : rules.size());
    }

    @EventListener
    public void onBridgeRuleDeleted(BridgeRuleDeletedEvent event) {
        ruleCacheDataHelper.delBridgeEnabledRule(event.getEventSource().getAppId(),event.getEventSource().getDirection(),event.getEventSource().getRuleCode());
        log.info("[BridgeRuleChangedEventListener] onBridgeRuleDeleted ruleId={} → delAllBridgeEnabledRules", event.getEventSource().getRuleId());
    }

    // ============================== 数据源 ==============================

    @EventListener
    public void onDataSourceChanged(DataSourceChangedEvent event) {
        Long dsId = event.getEventSource().getDataSourceId();
        // Service 内部已 BeanPlusUtil 转 VO,helper 不再持有 entity 引用
        DataSourceResultVO ds = dataSourceService.getDataSourceVO(dsId);
        if (ds != null) {
            // helper 内部把 ResultVO 再转 CacheVO 写入缓存
            ruleCacheDataHelper.setDataSource(dsId, ds);
        } else {
            // DB 已找不到(竞态),退化为 del
            ruleCacheDataHelper.delDataSource(dsId);
        }
        log.info("[BridgeRuleChangedEventListener] onDataSourceChanged dsId={} → refresh ds cache", dsId);
    }

    @EventListener
    public void onDataSourceDeleted(DataSourceDeletedEvent event) {
        Long dsId = event.getEventSource().getDataSourceId();
        ruleCacheDataHelper.delDataSource(dsId);
        log.info("[BridgeRuleChangedEventListener] onDataSourceDeleted dsId={} → delDataSource", dsId);
    }

    // ============================== 规则脚本 ==============================

    /**
     * 脚本变更事件 ── 直查 DB 拿当前脚本(含 enable + scriptContent),按 enable 决定 register / clear。
     * <p>cacheKey 通过事件源的 4 个 identity 字段重建 ── 走 {@link ScriptIdentifier#buildCacheKey}
     * 复用既有 {@code GroovyScriptCacheKeyBuilder}。allowCover=true 强制覆盖旧缓存(变更场景就是要刷新)。
     */
    @EventListener
    public void onGroovyScriptChanged(RuleGroovyScriptChangedEvent event) {
        RuleGroovyScriptChangedEventSource src = event.getEventSource();
        // 设备上行前置转换脚本(scriptType=topicInboundTransform)走 HASH 桶刷新(产品+版本),不走 String key 注册路径
        if (GroovyScriptCacheKeyBuilder.TRANSFORM_SCRIPT_TYPE.equals(src.getScriptType())) {
            refreshTransformBucket(src);
            return;
        }
        CacheKey cacheKey = buildScriptCacheKey(src);
        try {
            // 直接查 DB 拿当前脚本(走 service 而非 cache,避免事件监听里再触发 cache miss 形成循环刷新)
            RuleGroovyScript script = ruleGroovyScriptService.getById(src.getScriptId());
            if (script == null) {
                // DB 已找不到(竞态),退化为 clear
                registerScriptHelper.clear(cacheKey);
                log.info("[BridgeRuleChangedEventListener] onGroovyScriptChanged scriptId={} not found, fallback clear", src.getScriptId());
                return;
            }
            if (Boolean.TRUE.equals(script.getEnable())) {
                registerScriptHelper.registerScript(cacheKey, script.getScriptContent(), true);
                log.info("[BridgeRuleChangedEventListener] onGroovyScriptChanged scriptId={} → register", src.getScriptId());
            } else {
                registerScriptHelper.clear(cacheKey);
                log.info("[BridgeRuleChangedEventListener] onGroovyScriptChanged scriptId={} → clear (disabled)", src.getScriptId());
            }
        } catch (Exception e) {
            log.error("[BridgeRuleChangedEventListener] onGroovyScriptChanged scriptId={} failed", src.getScriptId(), e);
        }
    }

    /**
     * 脚本删除事件 ── DB 行已删除,只能靠事件源的 4 个 identity 字段重建 cacheKey 后清理。
     */
    @EventListener
    public void onGroovyScriptDeleted(RuleGroovyScriptDeletedEvent event) {
        RuleGroovyScriptChangedEventSource src = event.getEventSource();
        // 前置转换脚本删除:同样按 (产品+版本) 整桶刷新,被删脚本不在启用列表中即自然移除
        if (GroovyScriptCacheKeyBuilder.TRANSFORM_SCRIPT_TYPE.equals(src.getScriptType())) {
            refreshTransformBucket(src);
            return;
        }
        CacheKey cacheKey = buildScriptCacheKey(src);
        try {
            registerScriptHelper.clear(cacheKey);
            log.info("[BridgeRuleChangedEventListener] onGroovyScriptDeleted scriptId={} → clear", src.getScriptId());
        } catch (Exception e) {
            log.error("[BridgeRuleChangedEventListener] onGroovyScriptDeleted scriptId={} failed", src.getScriptId(), e);
        }
    }

    /**
     * 设备上行前置转换脚本桶刷新 ── 按 (渠道 + 产品 + 产品版本) 重查启用脚本,
     * 聚成 {@code topic 模式 → 脚本内容} 整桶刷新到 Redis HASH。变更/删除/启停统一走此路径。
     */
    private void refreshTransformBucket(RuleGroovyScriptChangedEventSource src) {
        String channel = src.getChannelCode();
        String product = src.getProductIdentification();
        String version = src.getObjectVersion();
        List<RuleGroovyScript> scripts = ruleGroovyScriptService.listEnabledTransformScripts(channel, product, version);
        Map<String, TransformScriptEntry> topicToEntry = scripts.stream()
            .filter(s -> StrUtil.isNotBlank(s.getTopicPattern()) && StrUtil.isNotBlank(s.getScriptContent()))
            .collect(Collectors.toMap(RuleGroovyScript::getTopicPattern,
                s -> new TransformScriptEntry(s.getScriptContent(), s.getExtendParams()), (a, b) -> a));
        ruleCacheDataHelper.setTransformScriptBucket(channel, product, version, topicToEntry);
        log.info("[BridgeRuleChangedEventListener] refresh transform bucket channel={} product={} version={} size={}",
            channel, product, version, topicToEntry.size());
    }

    /**
     * 用事件源 4 个 identity 字段重建 {@code GroovyScriptCacheKeyBuilder} cache key。
     */
    private CacheKey buildScriptCacheKey(RuleGroovyScriptChangedEventSource src) {
        RuleGroovyScriptQuery query = BeanPlusUtil.toBeanIgnoreError(src, RuleGroovyScriptQuery.class);
        return ScriptIdentifier.buildCacheKey(query);
    }
}
