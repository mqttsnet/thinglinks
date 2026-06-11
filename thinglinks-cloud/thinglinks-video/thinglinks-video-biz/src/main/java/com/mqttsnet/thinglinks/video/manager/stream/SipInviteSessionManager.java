package com.mqttsnet.thinglinks.video.manager.stream;

import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.cache.video.sip.SipInviteSessionCallCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.SsrcTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SIP INVITE 会话查询 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>调用方必须在调用前完成 {@code ContextUtil.setTenantId(...)}，本类不再持 {@code @DS}。
 *
 * @author mqttsnet
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class SipInviteSessionManager {

    private final CachePlusOps cachePlusOps;

    public SsrcTransaction getSsrcTransactionByCallId(String deviceIdentification, String callId) {
        CacheHashKey cacheHashKey = SipInviteSessionCallCacheKeyBuilder.builder(deviceIdentification, callId);
        CacheResult<Object> result = cachePlusOps.hGet(cacheHashKey);
        if (result == null || result.getRawValue() == null) {
            return null;
        }
        return BeanPlusUtil.toBeanIgnoreError(result.getRawValue(), SsrcTransaction.class);
    }
}
