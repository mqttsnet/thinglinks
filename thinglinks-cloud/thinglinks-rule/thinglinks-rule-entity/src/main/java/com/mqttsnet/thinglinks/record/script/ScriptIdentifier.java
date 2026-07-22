package com.mqttsnet.thinglinks.record.script;

import com.google.common.base.Joiner;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.common.cache.rule.groovy.GroovyScriptCacheKeyBuilder;
import com.mqttsnet.thinglinks.vo.query.script.RuleGroovyScriptQuery;

/**
 * ============================================================================
 * Description:
 * <p>
 * 脚本唯一标识 Record
 *
 * @param scriptType             脚本类型（必填）
 * @param channelCode            渠道编码（必填）
 * @param productIdentification  产品标识（必填）
 * @param topicPattern           主题模式（必填,动态参数）
 *                               ============================================================================
 * @author Sun Shihuan
 * @version 1.0.0
 * @email
 * @date 2025/4/15 14:51
 */
public record ScriptIdentifier(
        String scriptType,
        String channelCode,
        String productIdentification,
        String topicPattern
) {

    /**
     * 构造器校验
     */
    public ScriptIdentifier {
        // 必填字段校验
        ArgumentAssert.notBlank(scriptType, "脚本类型不能为空");
        ArgumentAssert.notBlank(channelCode, "渠道编码不能为空");
        ArgumentAssert.notBlank(productIdentification, "产品标识不能为空");
        ArgumentAssert.notBlank(topicPattern, "主题模式不能为空");
    }

    // 核心方法 -----------------------------------------------------

    /**
     * 生成 CacheKey
     *
     * @param query 查询对象
     * @return {@link CacheKey}
     */
    public static CacheKey buildCacheKey(RuleGroovyScriptQuery query) {
        String keyPart = Joiner.on(StrPool.COLON)
                .join(query.getScriptType(), query.getChannelCode(),
                        query.getProductIdentification(), query.getTopicPattern());
        return GroovyScriptCacheKeyBuilder.builder(keyPart);
    }

    // 转换方法 -----------------------------------------------------

    /**
     * 从查询对象转换
     */
    public static ScriptIdentifier fromQuery(RuleGroovyScriptQuery query) {
        ArgumentAssert.notNull(query, "查询对象不能为空");
        return new ScriptIdentifier(
                query.getScriptType(),
                query.getChannelCode(),
                query.getProductIdentification(),
                query.getTopicPattern()
        );
    }
}
