package com.mqttsnet.thinglinks.cache.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.cache.utils.CachePlusUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataBridgeCacheVO;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataSourceCacheVO;
import com.mqttsnet.thinglinks.common.cache.rule.bridge.BridgeRuleEnabledCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.rule.bridge.DataSourceCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.rule.groovy.GroovyScriptCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.rule.groovy.TransformScriptEntry;
import com.mqttsnet.thinglinks.vo.result.bridge.DataBridgeResultVO;
import com.mqttsnet.thinglinks.vo.result.bridge.DataSourceResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rule 域缓存操作类(对齐 LinkCacheDataHelper)。
 * 调用层级:Helper → caller 注入 dbLoader lambda → 域内 Service。用 cache-aside + 显式 loader 注入避免 helper 反向持有 Service 形成构造期环;caller 必须是 Component/Listener/Matcher 等非 Service 角色,不要在 Service 里直接调本 helper。
 *
 * @author mqttsnet
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RuleCacheDataHelper {

    private static final int DEFAULT_PRIORITY = 100;

    private final CachePlusOps cachePlusOps;
    /**
     * read-through 统一入口 ── {@link CachePlusUtil#getOrLoad} 包装 cachePlusOps + 类型转换 + Optional 返值.
     */
    private final CachePlusUtil cachePlusOpsUtil;

    // ============================== Groovy 脚本内容 ==============================

    /**
     * 仅查 Redis 不回源 ── 用于"存在性检查"场景。命中返脚本内容,缓存无返 {@link Optional#empty()}。
     *
     * @param cacheKey 缓存键
     * @return 脚本内容;缓存无返 {@link Optional#empty()}
     */
    public Optional<Object> getScriptContent(CacheKey cacheKey) {
        return Optional.ofNullable(cachePlusOps.get(cacheKey))
                .map(CacheResult::getRawValue);
    }

    /**
     * cache-aside 取脚本内容,miss 时回源 DB 并自动回填({@link CachePlusUtil#getOrLoad})。
     * cacheNullValues=false:DB 也没时不缓存 null,下次重新查。命中或回源命中返脚本内容,DB 也无返 {@link Optional#empty()}。
     *
     * @param cacheKey 缓存键
     * @param dbLoader 回源函数
     * @return 脚本内容;DB 也无返 {@link Optional#empty()}
     */
    public Optional<Object> getScriptContent(CacheKey cacheKey, Function<CacheKey, Object> dbLoader) {
        return cachePlusOpsUtil.getOrLoad(cacheKey, dbLoader, Object.class, false);
    }

    public void setScriptContent(CacheKey cacheKey, String content) {
        cachePlusOps.del(cacheKey);
        cachePlusOps.set(cacheKey, content);
    }

    public Long delScriptContent(CacheKey cacheKey) {
        return cachePlusOps.del(cacheKey);
    }

    // ============================== 设备上行前置转换脚本 HASH 桶 ==============================

    /**
     * 刷新某「产品 + 产品版本」的前置转换脚本 HASH 桶 ── del 旧桶 + hMSet(field=topic 模式,value=脚本内容)。
     * <p>供脚本 CRUD 变更事件用:每次变更按 (channel, product, version) 重查启用脚本整桶刷新,
     * 与桥接规则桶刷新同模式。topicToScript 为空则只删桶(该产品版本已无启用转换脚本)。
     *
     * @param channelCode           渠道编码(mqtt / webSocket)
     * @param productIdentification 产品标识
     * @param productVersionNo      产品发布版本号
     * @param topicToEntry          topic 模式 → {脚本内容 + 扩展参数}(值以 JSON 存入 HASH)
     */
    public void setTransformScriptBucket(String channelCode, String productIdentification,
                                         String productVersionNo, Map<String, TransformScriptEntry> topicToEntry) {
        if (StrUtil.hasBlank(channelCode, productIdentification, productVersionNo)) {
            return;
        }
        CacheKey bucketKey = GroovyScriptCacheKeyBuilder.transformHashKey(channelCode, productIdentification, productVersionNo);
        cachePlusOps.del(bucketKey);
        if (CollUtil.isEmpty(topicToEntry)) {
            log.info("Refresh transform script bucket EMPTY channel={} product={} version={}", channelCode, productIdentification, productVersionNo);
            return;
        }
        // 值序列化为 JSON 存入 HASH(field=topic 模式,value={content, extendParams})
        Map<String, String> fieldValues = new HashMap<>(topicToEntry.size());
        topicToEntry.forEach((topic, entry) -> fieldValues.put(topic, JSON.toJSONString(entry)));
        cachePlusOps.hMSet(bucketKey, fieldValues);
        log.info("Refresh transform script bucket channel={} product={} version={} count={}",
            channelCode, productIdentification, productVersionNo, fieldValues.size());
    }

    /**
     * 删除某「产品 + 产品版本」的前置转换脚本整桶。
     *
     * @param channelCode           渠道编码
     * @param productIdentification 产品标识
     * @param productVersionNo      产品发布版本号
     */
    public void delTransformScriptBucket(String channelCode, String productIdentification, String productVersionNo) {
        if (StrUtil.hasBlank(channelCode, productIdentification, productVersionNo)) {
            return;
        }
        cachePlusOps.del(GroovyScriptCacheKeyBuilder.transformHashKey(channelCode, productIdentification, productVersionNo));
        log.info("Del transform script bucket channel={} product={} version={}", channelCode, productIdentification, productVersionNo);
    }

    // ============================== 桥接规则 hash 缓存 ==============================

    /**
     * 取启用中的桥接规则缓存(命中 0 DB IO;miss 穿透 DB 并回填整桶)。按 priority 升序,永不返 null。
     *
     * @param appId     应用 ID
     * @param direction 桥接方向
     * @param dbLoader 回源函数返回 ResultVO 列表,Service 已 BeanPlusUtil 转 VO
     * @return 启用中的桥接规则列表,按 priority 升序
     */
    public List<DataBridgeCacheVO> getBridgeEnabledRules(String appId, String direction,
                                                        Function<Void, List<DataBridgeResultVO>> dbLoader) {
        if (StrUtil.hasBlank(appId, direction)) {
            return List.of();
        }
        CacheKey bucketKey = BridgeRuleEnabledCacheKeyBuilder.builder(appId, direction);
        Map<String, CacheResult<DataBridgeCacheVO>> result = cachePlusOps.hGetAll(bucketKey, k -> {
            List<DataBridgeResultVO> rules = dbLoader.apply(null);
            return CollUtil.isEmpty(rules) ? Map.of() : toCacheVoMap(rules);
        }, false);
        if (CollUtil.isEmpty(result)) {
            return List.of();
        }
        return result.values().stream()
                .map(CacheResult::getRawValue)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(r -> Optional.ofNullable(r.getPriority()).orElse(DEFAULT_PRIORITY)))
                .toList();
    }

    /**
     * 刷新 hash 桶 ── Changed 事件用:del 旧桶 + hMSet 新值。
     *
     * @param appId     应用 ID
     * @param direction 桥接方向
     * @param rules     新规则列表
     */
    public void setBridgeEnabledRulesBucket(String appId, String direction, List<DataBridgeResultVO> rules) {
        if (StrUtil.hasBlank(appId, direction)) {
            return;
        }
        CacheKey bucketKey = BridgeRuleEnabledCacheKeyBuilder.builder(appId, direction);
        cachePlusOps.del(bucketKey);
        if (CollUtil.isEmpty(rules)) {
            log.info("Refresh bridge rule bucket EMPTY appId={} direction={}", appId, direction);
            return;
        }
        Map<String, DataBridgeCacheVO> fieldValues = toCacheVoMap(rules);
        cachePlusOps.hMSet(bucketKey, fieldValues);
        log.info("Refresh bridge rule bucket appId={} direction={} count={}",
                appId, direction, fieldValues.size());
    }

    /**
     * ResultVO → CacheVO 转换 + 以 ruleCode 为 key 去重。
     *
     * @param rules 规则列表
     * @return 以 ruleCode 为 key 的缓存 VO 映射
     */
    private static Map<String, DataBridgeCacheVO> toCacheVoMap(List<DataBridgeResultVO> rules) {
        return rules.stream()
                .filter(r -> StrUtil.isNotBlank(r.getRuleCode()))
                .collect(Collectors.toMap(
                        DataBridgeResultVO::getRuleCode,
                        r -> BeanPlusUtil.toBeanIgnoreError(r, DataBridgeCacheVO.class),
                        (a, b) -> a));
    }

    /**
     * 单条规则失效(hDel 一个 field)。
     *
     * @param appId     应用 ID
     * @param direction 桥接方向
     * @param ruleCode  规则编码
     */
    public void delBridgeEnabledRule(String appId, String direction, String ruleCode) {
        if (StrUtil.hasBlank(appId, direction, ruleCode)) {
            return;
        }
        CacheHashKey key = BridgeRuleEnabledCacheKeyBuilder.buildHashFieldKey(appId, direction, ruleCode);
        cachePlusOps.hDel(key);
        log.info("hDel bridge rule field appId={} direction={} ruleCode={}", appId, direction, ruleCode);
    }

    /**
     * 桶级失效 ── 批量变更场景。
     *
     * @param appId     应用 ID
     * @param direction 桥接方向
     */
    public void delBridgeEnabledRulesBucket(String appId, String direction) {
        if (StrUtil.hasBlank(appId, direction)) {
            return;
        }
        cachePlusOps.del(BridgeRuleEnabledCacheKeyBuilder.builder(appId, direction));
        log.info("Del bridge rule bucket appId={} direction={}", appId, direction);
    }

    // ============================== 数据源缓存 ==============================

    /**
     * cache-aside 取数据源,miss 回源 DB(connectionJson/credentialJson 已是明文)。
     * dataSourceId 为 null 或 DB 无返 {@link Optional#empty()}。
     *
     * @param dataSourceId 数据源 ID
     * @param dbLoader     回源函数
     * @return 数据源缓存;dataSourceId 为 null 或 DB 无返 {@link Optional#empty()}
     */
    public Optional<DataSourceCacheVO> getDataSource(Long dataSourceId, Function<Long, DataSourceResultVO> dbLoader) {
        if (dataSourceId == null) {
            return Optional.empty();
        }
        CacheKey key = DataSourceCacheKeyBuilder.builder(dataSourceId);
        return cachePlusOpsUtil.getOrLoad(
            key,
            k -> {
                DataSourceResultVO vo = dbLoader.apply(dataSourceId);
                return vo == null ? null : BeanPlusUtil.toBeanIgnoreError(vo, DataSourceCacheVO.class);
            },
            DataSourceCacheVO.class,
            false);
    }

    /**
     * 刷新数据源条目 ── del + set。
     *
     * @param dataSourceId 数据源 ID
     * @param dataSource   数据源数据
     */
    public void setDataSource(Long dataSourceId, DataSourceResultVO dataSource) {
        if (dataSourceId == null || dataSource == null) {
            return;
        }
        CacheKey key = DataSourceCacheKeyBuilder.builder(dataSourceId);
        cachePlusOps.del(key);
        cachePlusOps.set(key, BeanPlusUtil.toBeanIgnoreError(dataSource, DataSourceCacheVO.class));
        log.info("Refresh data source cache dataSourceId={}", dataSourceId);
    }

    public void delDataSource(Long dataSourceId) {
        if (dataSourceId == null) {
            return;
        }
        cachePlusOps.del(DataSourceCacheKeyBuilder.builder(dataSourceId));
        log.info("Del data source cache dataSourceId={}", dataSourceId);
    }
}
