package com.mqttsnet.thinglinks.device.event.listener;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.device.enumeration.DeviceAclRuleLevelEnum;
import com.mqttsnet.thinglinks.device.event.DeviceAclRuleChangedEvent;
import com.mqttsnet.thinglinks.device.event.source.DeviceAclRuleChangedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ACL 规则变更后的缓存失效监听器。
 *
 * <p>用 AFTER_COMMIT 而非事务内清缓存:等父事务 commit 后再失效,回滚不触发,避免缓存与 DB 不一致;
 * fallbackExecution=true 兜底无事务调用方。设备级 HDEL 单 field,产品级 DEL 整个 hash。</p>
 *
 * @author mqttsnet
 * @since 2026-05-28
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceAclRuleCacheEvictListener {

    private final LinkCacheDataHelper linkCacheDataHelper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onChanged(DeviceAclRuleChangedEvent event) {
        DeviceAclRuleChangedEventSource src = event.getEventSource();
        if (src == null || StrUtil.isBlank(src.getProductIdentification())) {
            return;
        }
        try {
            if (src.getRuleLevel() == DeviceAclRuleLevelEnum.DEVICE_LEVEL
                && StrUtil.isNotBlank(src.getDeviceIdentification())) {
                linkCacheDataHelper.evictAclCacheByDevice(src.getProductIdentification(), src.getDeviceIdentification());
            } else {
                linkCacheDataHelper.evictAclCacheByProduct(src.getProductIdentification());
            }
        } catch (Exception e) {
            log.warn("[acl-cache] evict failed ruleId={} productId={} deviceId={}", src.getRuleId(), src.getProductIdentification(), src.getDeviceIdentification(), e);
        }
    }
}
