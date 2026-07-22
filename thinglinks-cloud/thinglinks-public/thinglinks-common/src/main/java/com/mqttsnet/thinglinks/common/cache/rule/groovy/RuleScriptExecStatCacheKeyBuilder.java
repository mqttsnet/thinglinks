package com.mqttsnet.thinglinks.common.cache.rule.groovy;

import java.time.Duration;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

/**
 * 规则脚本执行统计 KEY ── 按脚本唯一键(scriptType:channelCode:productIdentification:topicPattern)聚一个 HASH,
 * field = total / success / fail,生命周期累计计数(类似 mqs 数据上报统计)。
 *
 * <p>{@code rule:{tenantId}:groovy_script_exec_stat:number:{scriptUniqueKey}} -> Hash{total, success, fail}
 * <p>记录侧(执行)与读取侧(详情)都用同一 scriptUniqueKey 拼 KEY,严格一致 ──
 * 详情可由实体 4 身份字段经 {@code RuleGroovyScriptResultVO#buildOnlyKey()} 还原同一串。
 *
 * @author mqttsnet
 */
public class RuleScriptExecStatCacheKeyBuilder implements CacheKeyBuilder {

    /**
     * 整桶 KEY(hGetAll 读取 total/success/fail 三字段)。
     *
     * @param scriptUniqueKey 脚本唯一键(scriptType:channelCode:productIdentification:topicPattern)
     * @return 整桶 {@link CacheKey}
     */
    public static CacheKey build(String scriptUniqueKey) {
        return new RuleScriptExecStatCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).key(scriptUniqueKey);
    }

    /**
     * 单 field KEY(hIncrBy 对某个计数 +1)。
     *
     * @param scriptUniqueKey 脚本唯一键
     * @param field           字段(total / success / fail)
     * @return field 维度 {@link CacheHashKey}
     */
    public static CacheHashKey buildHashField(String scriptUniqueKey, String field) {
        return new RuleScriptExecStatCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashFieldKey(field, scriptUniqueKey);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Rule.GROOVY_SCRIPT_EXEC_STAT;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.RULE;
    }

    @Override
    public String getField() {
        return SuperEntity.ID_FIELD;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.number;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofDays(365L);
    }
}
