package com.mqttsnet.thinglinks.product.event.listener;

import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.product.event.ProductCacheEvictEvent;
import com.mqttsnet.thinglinks.product.event.source.ProductCacheEvictSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 产品基础信息缓存失效监听器。
 *
 * <p>用 {@link TransactionPhase#AFTER_COMMIT} 保证 DB 已落库再失效缓存,下次读 read-through 回填最新值;
 * fallbackExecution=true 兼容无事务调用方。</p>
 *
 * <p>租户上下文:deleteProductCacheVO(@DS(BASE_TENANT) + cacheKey 拼 tenantId)依赖 ThreadLocal,
 * 异步线程可能缺失,故当前线程无租户时从事件 contextMap 恢复,且只清自己恢复的。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCacheEvictListener {

    private final LinkCacheDataHelper linkCacheDataHelper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEvict(ProductCacheEvictEvent event) {
        ProductCacheEvictSource src = event.getEventSource();
        if (src == null || StrUtil.isBlank(src.getProductIdentification())) {
            return;
        }
        boolean restored = false;
        if (ContextUtil.getTenantId() == null && CollUtil.isNotEmpty(src.getContextMap())) {
            ContextUtil.setLocalMap(src.getContextMap());
            restored = true;
        }
        try {
            linkCacheDataHelper.deleteProductCacheVO(src.getProductIdentification());
        } catch (Exception e) {
            log.warn("[product-cache-evict] failed productIdentification={} err={}",
                    src.getProductIdentification(), e.getMessage());
        } finally {
            if (restored) {
                ContextUtil.remove();
            }
        }
    }
}
