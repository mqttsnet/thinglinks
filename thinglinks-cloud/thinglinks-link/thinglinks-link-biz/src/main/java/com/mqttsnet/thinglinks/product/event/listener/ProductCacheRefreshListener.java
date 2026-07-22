package com.mqttsnet.thinglinks.product.event.listener;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.product.event.ProductModelChangedEvent;
import com.mqttsnet.thinglinks.product.event.handler.ProductInfoUpdatedCacheHandler;
import com.mqttsnet.thinglinks.product.event.handler.ProductModelUpdatedCacheHandler;
import com.mqttsnet.thinglinks.product.event.source.ProductModelChangedSource;
import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductChangeTargetTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 产品缓存刷新监听器 —— 消费 {@link ProductModelChangedEvent} 异步刷新缓存。
 * 产品信息维度 → 刷产品缓存;服务 / 属性 / 命令维度 → 刷物模型缓存。
 *
 * <p>用 {@link TransactionalEventListener}(AFTER_COMMIT) 而非 @EventListener:主事务回滚时不要刷缓存,
 * 否则缓存里会写入"从未持久化的中间状态"产生脏数据;fallbackExecution=true 兼容无事务调用方(如 Job 内部)。</p>
 *
 * <p>用具名 linkDefaultExecutor 而非 ForkJoinPool.commonPool():命名清晰、统一资源池不抢占 JVM 全局 commonPool、
 * 异常进 Spring 异步上下文可追踪。</p>
 *
 * <p>主线程 {@link ContextUtil#getLocalMap()} 引用直接透传到异步线程(同属一次请求的延续,异步刷缓存需要用户 / 租户 / traceId);
 * ctx 在 publishEvent 之后到请求结束之间不会再 mutate,无 race 风险。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCacheRefreshListener {

    private final ProductInfoUpdatedCacheHandler productInfoUpdatedCacheHandler;
    private final ProductModelUpdatedCacheHandler productModelUpdatedCacheHandler;
    @Qualifier("linkDefaultExecutor")
    private final ThreadPoolExecutor linkDefaultExecutor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProductModelChanged(ProductModelChangedEvent event) {
        ProductModelChangedSource src = event.getEventSource();
        if (src == null || src.getProductIdentification() == null) {
            return;
        }
        String pid = src.getProductIdentification();
        // 多租户隔离 fail-fast:DsConstant.BASE_TENANT 走 SPEL 取 ThreadLocal 数据源名,
        // tenantId 缺失时 dynamic-datasource 静默 fallback 到 primary "0" 默认库,跨租户串味。
        if (ContextUtil.getTenantId() == null) {
            log.error("[ProductCacheRefresh] skip refresh: no tenantId in ContextUtil"
                + "(可能事件由系统线程发出), productIdentification={}", pid);
            return;
        }
        boolean productInfo = Optional.ofNullable(src.getTargetType())
            .filter(ProductChangeTargetTypeEnum.PRODUCT_INFO::equals)
            .isPresent();
        Map<String, String> ctx = ContextUtil.getLocalMap();
        CompletableFuture.runAsync(() -> {
            try {
                ContextUtil.setLocalMap(ctx);
                if (productInfo) {
                    productInfoUpdatedCacheHandler.handleProductUpdatedCache(pid);
                } else {
                    productModelUpdatedCacheHandler.handleProductModelUpdatedCache(pid);
                }
            } finally {
                ContextUtil.remove();
            }
        }, linkDefaultExecutor).exceptionally(ex -> {
            log.error("[ProductCacheRefresh] failed productIdentification={}", pid, ex);
            return null;
        });
    }
}
