package com.mqttsnet.thinglinks.common.cache.rule.trigger;

import java.time.Duration;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

/**
 * 规则引擎事件触发链路缓存 key 工厂。
 *
 * <p><b>Key 一览</b>(均按 {@link CacheKeyBuilder} 规范自动带租户前缀):
 * <pre>
 *   rule_trigger_index:bucket:obj:product:{conditionType}:{product}          触发索引产品桶(hash)
 *   rule_trigger_index:bucket:obj:device:{conditionType}:{product}:{device}  触发索引设备桶(hash)
 *   rule_trigger_details:ruleIdentification:obj:{ruleIdentification}         规则详情缓存
 *   rule_latest_snapshot:device:obj:{product}:{device}                       设备最新物模型快照
 *   rule_anti_shake:cnt:number:{conditionUuid}:{device}                      防抖窗口计数器
 *   rule_anti_shake:first:string:{conditionUuid}:{device}                    防抖窗口首条报文快照
 *   rule_action_cooldown:condition:number:{rule}:{conditionUuid}:{device}    动作冷却闸
 * </pre>
 *
 * <p><b>一致性模型(B1)</b>:索引与详情走"纯 TTL 过期"而非变更事件失效——规则配置变更
 * 最迟 {@link #INDEX_BUCKET_TTL}/{@link #DETAILS_TTL} 后生效,换取规则 CUD 链路零侵入;
 * 变更事件精确失效留待下一期。
 *
 * @author mqttsnet
 */
public final class RuleTriggerCacheKeys {

    /** 触发索引桶 TTL ── 兼作"规则条件改绑产品/设备后旧桶残留"的自愈上限 */
    public static final Duration INDEX_BUCKET_TTL = Duration.ofMinutes(5);
    /** 规则详情缓存 TTL ── 规则名称/条件/动作变更的最大生效延迟 */
    public static final Duration DETAILS_TTL = Duration.ofSeconds(60);
    /** 设备最新快照 TTL ── 与数据收集池同语义:设备静默 24h 后快照过期 */
    public static final Duration LATEST_SNAPSHOT_TTL = Duration.ofHours(24);

    /** 触发索引空桶占位 field(Redis hash 无法存在零 field 的 key,空结论需占位防穿透) */
    public static final String EMPTY_BUCKET_FIELD = "__empty__";

    private RuleTriggerCacheKeys() {
    }

    /** 触发索引产品桶(收"全部设备"范围规则) */
    public static CacheKey triggerIndexProductBucket(Integer conditionType, String productIdentification) {
        return builder(CacheKeyTable.Rule.RULE_TRIGGER_INDEX, "bucket", CacheKeyBuilder.ValueType.obj,
                INDEX_BUCKET_TTL).key("product", conditionType, productIdentification);
    }

    /** 触发索引设备桶(收绑定具体设备的规则) */
    public static CacheKey triggerIndexDeviceBucket(Integer conditionType, String productIdentification,
                                                    String deviceIdentification) {
        return builder(CacheKeyTable.Rule.RULE_TRIGGER_INDEX, "bucket", CacheKeyBuilder.ValueType.obj,
                INDEX_BUCKET_TTL).key("device", conditionType, productIdentification, deviceIdentification);
    }

    /** 索引桶内单 field 定位(占位/规则 field 写入用) */
    public static CacheHashKey triggerIndexField(CacheKey bucket, String field) {
        return new CacheHashKey(bucket.getKey(), field, bucket.getExpire());
    }

    /** 规则详情缓存 */
    public static CacheKey ruleDetails(String ruleIdentification) {
        return builder(CacheKeyTable.Rule.RULE_TRIGGER_DETAILS, "ruleIdentification", CacheKeyBuilder.ValueType.obj,
                DETAILS_TTL).key(ruleIdentification);
    }

    /** 设备最新物模型快照 */
    public static CacheKey latestSnapshot(String productIdentification, String deviceIdentification) {
        return builder(CacheKeyTable.Rule.RULE_LATEST_SNAPSHOT, "device", CacheKeyBuilder.ValueType.obj,
                LATEST_SNAPSHOT_TTL).key(productIdentification, deviceIdentification);
    }

    /** 防抖窗口计数器(TTL=防抖窗口秒数) */
    public static CacheKey antiShakeCounter(String conditionUuid, String deviceIdentification, long windowSeconds) {
        return builder(CacheKeyTable.Rule.RULE_ANTI_SHAKE, "cnt", CacheKeyBuilder.ValueType.number,
                Duration.ofSeconds(windowSeconds)).key(conditionUuid, deviceIdentification);
    }

    /** 防抖窗口首条报文快照(TTL=防抖窗口秒数) */
    public static CacheKey antiShakeFirst(String conditionUuid, String deviceIdentification, long windowSeconds) {
        return builder(CacheKeyTable.Rule.RULE_ANTI_SHAKE, "first", CacheKeyBuilder.ValueType.string,
                Duration.ofSeconds(windowSeconds)).key(conditionUuid, deviceIdentification);
    }

    /** 动作冷却闸(TTL=冷却秒数) */
    public static CacheKey actionCooldown(String ruleIdentification, String conditionUuid,
                                          String deviceIdentification, long cooldownSeconds) {
        return builder(CacheKeyTable.Rule.RULE_ACTION_COOLDOWN, "condition", CacheKeyBuilder.ValueType.number,
                Duration.ofSeconds(cooldownSeconds)).key(ruleIdentification, conditionUuid, deviceIdentification);
    }

    private static TriggerKeyBuilder builder(String table, String field, CacheKeyBuilder.ValueType valueType,
                                             Duration expire) {
        return new TriggerKeyBuilder(table, field, valueType, expire)
                .setTenantId(ContextUtil.getTenantId());
    }

    private static final class TriggerKeyBuilder implements CacheKeyBuilder {
        private final String table;
        private final String field;
        private final ValueType valueType;
        private final Duration expire;
        private Long tenantId;

        private TriggerKeyBuilder(String table, String field, ValueType valueType, Duration expire) {
            this.table = table;
            this.field = field;
            this.valueType = valueType;
            this.expire = expire;
        }

        @Override
        public String getTenant() {
            return String.valueOf(tenantId);
        }

        @Override
        public TriggerKeyBuilder setTenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        @Override
        public String getTable() {
            return table;
        }

        @Override
        public String getModular() {
            return CacheKeyModular.RULE;
        }

        @Override
        public String getField() {
            return field;
        }

        @Override
        public ValueType getValueType() {
            return valueType;
        }

        @Override
        public Duration getExpire() {
            return expire;
        }
    }
}
