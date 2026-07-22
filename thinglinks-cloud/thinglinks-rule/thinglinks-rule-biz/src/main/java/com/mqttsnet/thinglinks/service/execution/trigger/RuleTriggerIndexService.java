package com.mqttsnet.thinglinks.service.execution.trigger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.common.cache.rule.trigger.RuleTriggerCacheKeys;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DeviceActionConditionGroupDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DevicePropertiesConditionGroupDTO;
import com.mqttsnet.thinglinks.entity.linkage.Rule;
import com.mqttsnet.thinglinks.entity.linkage.RuleCondition;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionStatusEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionTypeEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.RuleStatusEnum;
import com.mqttsnet.thinglinks.service.linkage.RuleConditionService;
import com.mqttsnet.thinglinks.service.linkage.RuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 规则触发索引 ── 设备事件到候选规则的反查(两级 hash 桶,读穿 + 进程内 TTL 缓存)。
 *
     * <p><b>桶结构</b>:产品桶收"全部设备"范围规则(条件左值 device=all),
 * 设备桶收绑定具体设备的规则;消费端取两桶并集。空结论写 NullVal 占位防穿透。
 *
 * <p><b>一致性</b>:纯 TTL 模型 ── Redis 桶 {@link RuleTriggerCacheKeys#INDEX_BUCKET_TTL},
 * 进程内缓存 {@code thinglinks.rule.trigger.index-local-cache-seconds}(默认 30s,
 * 高吞吐下把同产品全部消息的 HGETALL 收敛为每实例每 TTL 一次,消除热 key)。
 * 规则 CUD 后最迟一个桶 TTL 生效;条件改绑产品后的旧桶残留 field 只造成无效评估
 * (评估时详情已是新条件,事件不匹配即短路),随桶过期自愈。
 *
 * <p><b>回源</b>:rule_condition 按 (conditionType + 启用 + conditionScheme LIKE product) 粗筛,
 * 内存解析 conditionScheme 精确匹配 (product, device) 维度,再按规则启用状态过滤。
 * LIKE 仅发生在桶 miss 时(每产品每 TTL 至多一次),不在消息热路径。
 *
 * @author mqttsnet
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class RuleTriggerIndexService {

    private final CachePlusOps cachePlusOps;
    private final RuleConditionService ruleConditionService;
    private final RuleService ruleService;

    /** 进程内索引缓存 TTL(秒);0=关闭本地缓存 */
    @Value("${thinglinks.rule.trigger.index-local-cache-seconds:30}")
    private long localCacheSeconds;

    /** 进程内缓存条数上限,超限整体清空兜底(索引项极小,正常远达不到) */
    private static final int LOCAL_CACHE_MAX_SIZE = 20000;

    private final ConcurrentHashMap<String, LocalEntry> localCache = new ConcurrentHashMap<>();

    private record LocalEntry(long expireAt, List<String> ruleIdentifications) {
    }

    /**
     * 反查事件命中的候选规则(产品桶 ∪ 设备桶),已按启用状态过滤,返回 ruleIdentification 列表。
     *
     * @param conditionType         条件类型(ConditionTypeEnum.value)
     * @param productIdentification 事件产品标识
     * @param deviceIdentification  事件设备标识
     * @return 候选规则标识列表(去重,永不返 null)
     */
    public List<String> findTriggeredRules(Integer conditionType, String productIdentification,
                                           String deviceIdentification) {
        if (conditionType == null || StrUtil.isBlank(productIdentification)) {
            return List.of();
        }
        Set<String> result = new LinkedHashSet<>(loadBucket(
                RuleTriggerCacheKeys.triggerIndexProductBucket(conditionType, productIdentification),
                key -> loadFromDb(conditionType, productIdentification, null)));
        if (StrUtil.isNotBlank(deviceIdentification)) {
            result.addAll(loadBucket(
                    RuleTriggerCacheKeys.triggerIndexDeviceBucket(conditionType, productIdentification, deviceIdentification),
                    key -> loadFromDb(conditionType, productIdentification, deviceIdentification)));
        }
        return new ArrayList<>(result);
    }

    /** 桶读取:本地缓存 → Redis(读穿 DB) */
    private List<String> loadBucket(CacheKey bucketKey, Function<CacheKey, List<String>> dbLoader) {
        String localKey = bucketKey.getKey();
        long now = System.currentTimeMillis();
        if (localCacheSeconds > 0) {
            LocalEntry entry = localCache.get(localKey);
            if (entry != null && entry.expireAt() > now) {
                return entry.ruleIdentifications();
            }
        }
        List<String> rules = readBucketFromRedis(bucketKey, dbLoader);
        if (localCacheSeconds > 0) {
            if (localCache.size() > LOCAL_CACHE_MAX_SIZE) {
                localCache.clear();
            }
            localCache.put(localKey, new LocalEntry(now + localCacheSeconds * 1000, rules));
        }
        return rules;
    }

    private List<String> readBucketFromRedis(CacheKey bucketKey, Function<CacheKey, List<String>> dbLoader) {
        // cacheNullValues=true:空结论写 NullVal 占位落桶,后续零穿透;读侧 getValue() 把占位归一为 null 过滤
        Map<String, CacheResult<String>> result = cachePlusOps.hGetAll(bucketKey, k -> {
            List<String> ruleIds = dbLoader.apply(k);
            return CollUtil.isEmpty(ruleIds)
                    ? Collections.<String, String>singletonMap(RuleTriggerCacheKeys.EMPTY_BUCKET_FIELD, null)
                    : ruleIds.stream().collect(Collectors.toMap(Function.identity(), Function.identity(), (a, b) -> a));
        }, true);
        // hGetAll(loader) 不会给回源桶设 TTL,这里显式补上,保证残留自愈与"纯 TTL 一致性"成立
        cachePlusOps.expire(bucketKey);
        if (CollUtil.isEmpty(result)) {
            return List.of();
        }
        return result.values().stream()
                .map(CacheResult::getValue)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * DB 回源:rule_condition LIKE 粗筛 → conditionScheme 精确匹配 → 规则启用过滤。
     *
     * @param deviceIdentification null=产品桶("全部设备"规则,条件左值 device=all);非空=设备桶
     */
    private List<String> loadFromDb(Integer conditionType, String productIdentification, String deviceIdentification) {
        List<RuleCondition> conditions = ruleConditionService.list(Wraps.<RuleCondition>lbQ()
                .eq(RuleCondition::getConditionType, conditionType)
                .eq(RuleCondition::getStatus, ConditionStatusEnum.ENABLED.getValue())
                .like(RuleCondition::getConditionScheme, productIdentification));
        if (CollUtil.isEmpty(conditions)) {
            return List.of();
        }
        List<Long> ruleIds = conditions.stream()
                .filter(c -> schemeMatches(conditionType, c.getConditionScheme(), productIdentification, deviceIdentification))
                .map(RuleCondition::getRuleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(ruleIds)) {
            return List.of();
        }
        List<Rule> rules = ruleService.listByIds(ruleIds);
        List<String> matched = rules.stream()
                .filter(r -> Objects.equals(r.getStatus(), RuleStatusEnum.ACTIVATED.getValue()))
                .map(Rule::getRuleIdentification)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        log.info("[RuleTriggerIndex] rebuild bucket type={} product={} device={} rules={}",
                conditionType, productIdentification, deviceIdentification, matched);
        return matched;
    }

    /** 条件左值定位维度(产品 + 设备) */
    private record LeftParamRef(String productIdentification, String deviceIdentification) {
    }

    /**
     * conditionScheme 精确匹配 ── 按条件类型走对应的强类型 DTO 反序列化
     * (与策略评估侧同一结构,字段变更编译期可见,不做手工 JSON 取值)。
     */
    private boolean schemeMatches(Integer conditionType, String conditionScheme,
                                  String productIdentification, String deviceIdentification) {
        if (StrUtil.isBlank(conditionScheme)) {
            return false;
        }
        try {
            return extractLeftParams(conditionType, conditionScheme).stream()
                    .filter(ref -> Objects.equals(ref.productIdentification(), productIdentification))
                    .anyMatch(ref -> deviceIdentification == null
                            ? isAllDevice(ref.deviceIdentification())
                            : Objects.equals(ref.deviceIdentification(), deviceIdentification));
        } catch (Exception e) {
            log.warn("[RuleTriggerIndex] parse conditionScheme failed, skip. type={} err={}",
                    conditionType, e.getMessage());
            return false;
        }
    }

    /** 按条件类型解析条件组,拉平出全部左值 (product, device) 维度 */
    private List<LeftParamRef> extractLeftParams(Integer conditionType, String conditionScheme) {
        if (Objects.equals(conditionType, ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.getValue())) {
            List<DevicePropertiesConditionGroupDTO> groups =
                    JSON.parseArray(conditionScheme, DevicePropertiesConditionGroupDTO.class);
            return groups.stream()
                    .filter(g -> g != null && CollUtil.isNotEmpty(g.getConditions()))
                    .flatMap(g -> g.getConditions().stream())
                    .filter(c -> c != null && c.getLeftParam() != null)
                    .map(c -> new LeftParamRef(c.getLeftParam().getProductIdentification(),
                            c.getLeftParam().getDeviceIdentification()))
                    .toList();
        }
        if (Objects.equals(conditionType, ConditionTypeEnum.DEVICE_ACTION_TRIGGER.getValue())) {
            List<DeviceActionConditionGroupDTO> groups =
                    JSON.parseArray(conditionScheme, DeviceActionConditionGroupDTO.class);
            return groups.stream()
                    .filter(g -> g != null && CollUtil.isNotEmpty(g.getConditions()))
                    .flatMap(g -> g.getConditions().stream())
                    .filter(c -> c != null && c.getLeftParam() != null)
                    .map(c -> new LeftParamRef(c.getLeftParam().getProductIdentification(),
                            c.getLeftParam().getDeviceIdentification()))
                    .toList();
        }
        return List.of();
    }

    private boolean isAllDevice(String deviceIdentification) {
        return StrUtil.equals(BizConstant.ALL, deviceIdentification);
    }
}
