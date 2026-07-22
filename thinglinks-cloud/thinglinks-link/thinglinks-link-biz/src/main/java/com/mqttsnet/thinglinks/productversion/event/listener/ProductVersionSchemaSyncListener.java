package com.mqttsnet.thinglinks.productversion.event.listener;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.productpublishrecord.service.ProductPublishRecordService;
import com.mqttsnet.thinglinks.productversion.event.ProductVersionPublishedEvent;
import com.mqttsnet.thinglinks.productversion.event.ProductVersionPurgeRequestedEvent;
import com.mqttsnet.thinglinks.productversion.event.ProductVersionRolledBackEvent;
import com.mqttsnet.thinglinks.productversion.event.source.ProductVersionLifecycleEventSource;
import com.mqttsnet.thinglinks.productversion.publish.orchestrator.ProductVersionPublishOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 产品版本生命周期事件监听器 ── thin 事件入口层(接收事件 → 切租户上下文 → 异步丢线程池 → 调编排器),
 * 业务全部委托给 {@link ProductVersionPublishOrchestrator}(该编排器同时被 retry 兜底 job 复用,事件路径 /
 * 定时路径逻辑零漂移)。
 *
 * <p>必须 AFTER_COMMIT:编排器内 schema sync 要查 product_version 拿 canaryConfigJson + snapshot,若在
 * publish() 事务提交前触发,RC 隔离级别下 async 线程查不到刚写入的行会导致设备改绑跳过;fallbackExecution=true
 * 兜底无事务发事件的场景。</p>
 *
 * @author mqttsnet
 * @see ProductVersionPublishOrchestrator
 * @see ProductVersionPublishedEvent
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductVersionSchemaSyncListener {

    private final ProductVersionPublishOrchestrator orchestrator;
    /** 仅在 async 异常 fallback 路径用到(把异常写回 record),正常路径都走 orchestrator。 */
    private final ProductPublishRecordService productPublishRecordService;
    /** 微服务级共享异步线程池 ── 不用 JVM 全局 commonPool,避免抢占资源 + 排查难。 */
    @Qualifier("linkDefaultExecutor")
    private final ThreadPoolExecutor linkDefaultExecutor;

    /**
     * 发布事件 → orchestrator.runPublish(创建 TD super table + 按策略改绑设备 + 刷缓存)。
     *
     * @param event 产品版本发布事件
     * @return 异步执行结果(成功为 true)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public CompletableFuture<Boolean> onPublished(ProductVersionPublishedEvent event) {
        return runAsync(event.getSource(), orchestrator::runPublish, "publish");
    }

    /**
     * 回滚事件 → orchestrator.runRollback(原绑 sourceVersion 的设备批量改绑到 targetVersion)。
     *
     * @param event 产品版本回滚事件
     * @return 异步执行结果(成功为 true)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public CompletableFuture<Boolean> onRolledBack(ProductVersionRolledBackEvent event) {
        return runAsync(event.getSource(), orchestrator::runRollback, "rollback");
    }

    /**
     * 历史清理事件 → orchestrator.runPurge(DROP STABLE)。
     *
     * @param event 产品版本历史清理请求事件
     * @return 异步执行结果(成功为 true)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public CompletableFuture<Boolean> onPurge(ProductVersionPurgeRequestedEvent event) {
        return runAsync(event.getSource(), orchestrator::runPurge, "purge");
    }

    /**
     * 三个事件入口共享的异步外壳:租户上下文透传 → CompletableFuture 异步执行 → 异常 fallback。租户上下文
     * (ContextUtil 里的 tenantId / userId / orgId)是 ThreadLocal 跨线程要主动拷贝:调用线程读 ctx Map,
     * 异步线程开始时恢复,结束时 remove 防泄漏(线程复用)。
     *
     * @param src 版本生命周期事件源
     * @param action 委托给编排器的业务动作
     * @param tag 日志标签(publish / rollback / purge)
     * @return 异步执行结果(成功为 true,缺租户上下文或异常为 false)
     */
    private CompletableFuture<Boolean> runAsync(ProductVersionLifecycleEventSource src,
                                                Function<ProductVersionLifecycleEventSource, Boolean> action,
                                                String tag) {
        // 多租户隔离 fail-fast:DsConstant.BASE_TENANT = "#thread.thinglinks_base" 走 SPEL 取
        // ThreadLocal 数据源名,tenantId 缺失时 dynamic-datasource 静默 fallback 到 primary "0"
        // 默认库 → 跨租户数据串味(把 A 租户的 publish_record 写到默认库)。
        // 这里早返并 markFailed,让 record 状态可见(用户能在前端看到失败,而不是默写错库)。
        if (ContextUtil.getTenantId() == null) {
            String reason = "no tenantId in ContextUtil (event from system thread?), skip " + tag;
            log.error("[publish-listener] {} skip: {} source={}", tag, reason, src);
            productPublishRecordService.markFailed(src.getPublishRecordId(), reason);
            return CompletableFuture.completedFuture(false);
        }
        Map<String, String> ctx = ContextUtil.getLocalMap();
        return CompletableFuture.supplyAsync(() -> {
                try {
                    ContextUtil.setLocalMap(ctx);
                    return action.apply(src);
                } finally {
                    ContextUtil.remove();
                }
            }, linkDefaultExecutor)
            .exceptionally(ex -> {
                // exceptionally 跟 supplyAsync 同线程跑,但上面 finally 已 remove 了 ThreadLocal ──
                // 这里 markFailed 走 @DS(BASE_TENANT) 切库 SPEL 取不到 tenantId 会 fallback 到默认库,
                // 真实租户库的 record 永远停留在 RUNNING,用户看到"卡死"。重新 setLocalMap 让切库正确。
                try {
                    ContextUtil.setLocalMap(ctx);
                    log.error("[publish-listener] {} async failed source={}", tag, src, ex);
                    productPublishRecordService.markFailed(src.getPublishRecordId(), orchestrator.unwrap(ex));
                } finally {
                    ContextUtil.remove();
                }
                return false;
            });
    }
}
