package com.mqttsnet.thinglinks.common.cache.rule.groovy;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;


/**
 * <p>
 * 规则脚本 KEY
 * <p>
 * [服务模块名:]业务类型[:业务字段][:value类型][:脚本类型+渠道编码+产品标识+主题模式] -> obj
 * rule:def_groovy_script:脚本KEY:obj:1 -> {}
 *
 * @author mqttsnet
 * @date 2023/5/30 6:45 下午
 */
public class GroovyScriptCacheKeyBuilder implements CacheKeyBuilder {


    /**
     * 构建脚本缓存KEY (以下参数均不可为空)
     *
     * @param key 脚本类型+渠道编码+产品标识+主题模式
     * @return {@link CacheKey} 缓存KEY
     */
    public static CacheKey builder(String key) {
        return new GroovyScriptCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).key(key);
    }

    // ============================== 设备上行前置转换脚本 HASH 桶 ==============================
    // 按「产品 + 产品发布版本」聚成一个 HASH 桶:hash field = 主题模式(topic 模式,基础/自定义),
    // value = 脚本内容。运行时 mqs 据设备绑定的产品版本 HGETALL 整桶,内存逐条匹配 topic,命中即得脚本。
    // 维度约定(固定段,保证运行时可由设备信息直接拼出 KEY,无需额外配置):
    //   脚本类型=topicInboundTransform,渠道编码由上行协议定(mqtt/webSocket)。

    /** 前置转换脚本「脚本类型」固定标识(scriptType 维度),据此与存量 datas 脚本(dataProtocol)区分。 */
    public static final String TRANSFORM_SCRIPT_TYPE = "topicInboundTransform";

    /**
     * 设备上行前置转换脚本的 HASH 桶 KEY ── hash field = topic 模式,value = 脚本内容。
     * <p>CRUD 写入与运行时读取都用本方法拼 KEY,保证两侧严格一致。
     *
     * @param channelCode           渠道编码(mqtt / webSocket),与上行协议对齐
     * @param productIdentification 产品标识(产品标识维度)
     * @param productVersionNo      产品发布版本号(对应 {@code product_version.version_no};运行时取自设备 {@code boundProductVersionNo})
     * @return HASH 桶 {@link CacheKey}
     */
    public static CacheKey transformHashKey(String channelCode, String productIdentification, String productVersionNo) {
        String key = String.join(StrPool.COLON,
            TRANSFORM_SCRIPT_TYPE, channelCode, productIdentification, productVersionNo);
        return builder(key);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Rule.DEF_GROOVY_SCRIPT;
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
        return ValueType.string;
    }

}
