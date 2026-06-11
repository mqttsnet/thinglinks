package com.mqttsnet.thinglinks.card.cache.helper;

import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.card.vo.cache.channel.CardChannelTokenKeyCacheVO;
import com.mqttsnet.thinglinks.common.cache.card.channel.OneLinkTokenKeyCacheKeyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * -----------------------------------------------------------------------------
 * 文件名称: CardCacheDataHelper.java
 * -----------------------------------------------------------------------------
 * 描述:
 * CardCacheDataHelper
 * 物联卡基础数据缓存操作类
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * 修改历史:
 * 日期           作者          版本        描述
 * --------      --------     -------   --------------------
 * 2024-06-30    ShiHuan Sun   1.0        初始创建
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2024-06-30 18:26
 */
@Component
@Slf4j
public class CardCacheDataHelper {

    private final CachePlusOps cachePlusOps;

    public CardCacheDataHelper(CachePlusOps cachePlusOps) {
        this.cachePlusOps = cachePlusOps;
    }

    /**
     * 设置 移动渠道 TokenKey 缓存.
     *
     * @param cacheVO     缓存模型
     * @param channelName 渠道名称,必须不为 {@literal null}.
     * @deprecated 物联卡 OneLink 接入未上线,本 helper 当前**无业务调用方**.
     * 接入时需补全 read-through:cache miss 自动调 OneLink API 换取 token 并回写缓存
     * (参考 {@link com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper#getDeviceCacheVO} 模式).
     */
    @Deprecated(since = "2026-05-18", forRemoval = false)
    public void setChannelOneLinkTokenKeyCacheVO(CardChannelTokenKeyCacheVO cacheVO, String channelName) {
        CacheKey cacheKey = OneLinkTokenKeyCacheKeyBuilder.build(channelName);
        cachePlusOps.del(cacheKey);
        cachePlusOps.set(cacheKey, cacheVO);
    }

    /**
     * 获取 移动 TokenKey 缓存.
     *
     * @param channelName 渠道名称,必须不为 {@literal null}.
     * @return 渠道 TokenKey 缓存模型;不存在返 {@code null}
     * @deprecated 物联卡 OneLink 接入未上线,本方法当前**无业务调用方**(全工程 grep 0 结果).
     * <p>⚠️ <b>已知设计缺陷</b>:当前是 cache-only,token 失效后无任何兜底链路,功能上线前必须改为 read-through:
     * <pre>{@code
     * return cacheUtil.getOrLoad(cacheKey,
     *     (k) -> oneLinkApiClient.fetchToken(channelName),  // 失效自动换 token
     *     CardChannelTokenKeyCacheVO.class, false).orElse(null);
     * }</pre>
     */
    @Deprecated(since = "2026-05-18", forRemoval = false)
    public CardChannelTokenKeyCacheVO getChannelOneLinkTokenKeyCacheVO(String channelName) {
        CacheKey cacheKey = OneLinkTokenKeyCacheKeyBuilder.build(channelName);
        CacheResult<Object> objectCacheResult = cachePlusOps.get(cacheKey);
        if (objectCacheResult == null || objectCacheResult.getRawValue() == null) {
            log.warn("The channel token key is not existent");
            return null;
        }
        return BeanPlusUtil.toBeanIgnoreError(objectCacheResult.getRawValue(), CardChannelTokenKeyCacheVO.class);
    }


}
