package com.mqttsnet.thinglinks.video.gb28181.session;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.cache.utils.CacheUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.cache.video.sip.SipInviteSessionCallCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.config.UserSetting;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.SsrcTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 视频流session管理器，管理视频预览、预览回放的通信句柄
 *
 * @author mqttsnet
 */
@DS(DsConstant.BASE_TENANT)
@RequiredArgsConstructor
@Slf4j
@Component
public class SipInviteSessionManager {

    private final UserSetting userSetting;

    private final CachePlusOps cachePlusOps;

    private final CacheUtil cacheUtil;


    public SsrcTransaction getSsrcTransactionByCallId(String deviceIdentification, String callId) {
        CacheHashKey cacheHashKey = SipInviteSessionCallCacheKeyBuilder.builder(deviceIdentification, callId);
        CacheResult<Object> objectCacheResult = cachePlusOps.hGet(cacheHashKey);
        return BeanPlusUtil.toBeanIgnoreError(objectCacheResult.getRawValue(), SsrcTransaction.class);
    }
}
