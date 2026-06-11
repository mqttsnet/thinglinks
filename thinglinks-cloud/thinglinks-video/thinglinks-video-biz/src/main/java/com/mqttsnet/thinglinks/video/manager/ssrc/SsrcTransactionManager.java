package com.mqttsnet.thinglinks.video.manager.ssrc;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.cache.video.ssrc.SsrcTransactionCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.SsrcTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SSRC 事务 Redis 缓存管理器。
 * <p>
 * 管理 SIP INVITE 会话中的 SSRC 事务信息，使用 Hash 结构：
 * Key 为设备国标编号，Field 为 Call-ID，Value 为 SsrcTransaction 对象。
 * <p>
 * 本类只做 Redis 读写，不依赖 SIP 协议栈，因此放在 biz 层，供 biz-protocol / controller / boot-impl（执行器）共用。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsrcTransactionManager {

    private final CachePlusOps cachePlusOps;

    /**
     * 保存 SSRC 事务
     *
     * @param deviceIdentification    设备国标编号
     * @param transaction SSRC 事务信息
     */
    public void put(String deviceIdentification, SsrcTransaction transaction) {
        CacheHashKey fieldKey = SsrcTransactionCacheKeyBuilder.builder(deviceIdentification, transaction.getCallId());
        cachePlusOps.hSet(fieldKey, transaction);
        log.debug("保存SSRC事务: deviceIdentification={}, callId={}, ssrc={}", deviceIdentification, transaction.getCallId(), transaction.getSsrc());
    }

    /**
     * 根据设备编号和 Call-ID 获取事务
     *
     * @param deviceIdentification 设备国标编号
     * @param callId   SIP Call-ID
     * @return SSRC 事务，不存在时返回 null
     */
    public SsrcTransaction get(String deviceIdentification, String callId) {
        CacheHashKey fieldKey = SsrcTransactionCacheKeyBuilder.builder(deviceIdentification, callId);
        var result = cachePlusOps.hGet(fieldKey);
        if (result == null || result.getRawValue() == null) {
            return null;
        }
        return BeanPlusUtil.toBeanIgnoreError(result.getRawValue(), SsrcTransaction.class);
    }

    /**
     * 获取设备的所有 SSRC 事务
     *
     * @param deviceIdentification 设备国标编号
     * @return 事务列表，无数据时返回空列表
     */
    public List<SsrcTransaction> getAll(String deviceIdentification) {
        CacheHashKey hashKey = SsrcTransactionCacheKeyBuilder.builder(deviceIdentification);
        Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
        if (MapUtil.isEmpty(entries)) {
            return Collections.emptyList();
        }

        List<SsrcTransaction> transactions = new ArrayList<>(entries.size());
        for (CacheResult<Object> cacheResult : entries.values()) {
            if (cacheResult != null && cacheResult.getRawValue() != null) {
                var transaction = BeanPlusUtil.toBeanIgnoreError(cacheResult.getRawValue(), SsrcTransaction.class);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    /**
     * 删除指定事务
     *
     * @param deviceIdentification 设备国标编号
     * @param callId   SIP Call-ID
     */
    public void remove(String deviceIdentification, String callId) {
        CacheHashKey fieldKey = SsrcTransactionCacheKeyBuilder.builder(deviceIdentification, callId);
        cachePlusOps.hDel(fieldKey);
        log.debug("删除SSRC事务: deviceIdentification={}, callId={}", deviceIdentification, callId);
    }

    /**
     * 删除设备的所有事务
     *
     * @param deviceIdentification 设备国标编号
     */
    public void removeAll(String deviceIdentification) {
        CacheHashKey hashKey = SsrcTransactionCacheKeyBuilder.builder(deviceIdentification);
        cachePlusOps.del(hashKey);
        log.debug("删除设备所有SSRC事务: deviceIdentification={}", deviceIdentification);
    }

    /**
     * 根据流 ID 查找事务
     *
     * @param deviceIdentification 设备国标编号
     * @param stream   流 ID
     * @return 匹配的事务，不存在时返回 null
     */
    public SsrcTransaction getByStream(String deviceIdentification, String stream) {
        return getAll(deviceIdentification).stream()
                .filter(t -> stream.equals(t.getStream()))
                .findFirst()
                .orElse(null);
    }
}
