package com.mqttsnet.thinglinks.productversion.draft;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.mqttsnet.basic.cache.lock.DistributedLock;
import com.mqttsnet.basic.cache.lock.LockRunResult;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.lock.link.LinkLockKeyBuilder;
import com.mqttsnet.thinglinks.product.event.ProductModelChangedEvent;
import com.mqttsnet.thinglinks.product.event.source.ProductModelChangedSource;
import com.mqttsnet.thinglinks.productversion.service.ProductVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 产品草稿快照维护器:监听产品树变更事件(产品基础信息 / 服务 / 属性 / 命令 / 命令参数 CRUD),收到后调
 * {@link ProductVersionService#upsertDraft(String)} 刷新当前 DRAFT 行 snapshot,实现"未发布前 CRUD 持续累积进
 * 同一份草稿,发布才定型"。复用各 service impl CRUD 末尾发的 {@link ProductModelChangedEvent} 信号源。
 *
 * <p>关键约束:① 必须 AFTER_COMMIT,否则异步线程 upsertDraft 查 DB 时原事务未提交会拉到旧产品树漏掉本次变更;
 * fallbackExecution=true 兜底无活动事务。② upsertDraft 是 "不存在则 insert" 的非原子操作,并发(整树导入瞬间 N
 * 个事件)会建出多个 DRAFT,故按 productIdentification 加分布式锁串行化;tryLock 未命中即跳过(幂等),upsertDraft
 * 内部还有"多 DRAFT 自愈"二次兜底。③ 丢 {@link CompletableFuture#runAsync} 避免大产品树序列化阻塞主请求,
 * {@link ContextUtil#getLocalMap()} 跨线程传租户上下文;失败仅打 warn,靠下次 CRUD 或 publish 前强制 upsert 自愈。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
public class ProductSnapshotMaintainer {

    /** upsertDraft 失败后的最大重试次数。 */
    private static final int MAX_RETRY = 3;
    /** 重试基础退避(毫秒),实际退避 = BASE * 当前尝试次数。 */
    private static final long RETRY_BACKOFF_MS = 500L;

    /**
     * {@link ProductVersionService} 注入需要 @Lazy ── ProductVersionServiceImpl 通过
     * ProductQueryService 间接依赖产品树查询,而 ProductServiceImpl 也回头依赖事件链路,
     * 直接注入会触发循环依赖,Lazy 把首次访问延后到方法调用时才解析。
     */
    private final ProductVersionService productVersionService;
    private final DistributedLock distributedLock;
    private final ThreadPoolExecutor linkDefaultExecutor;

    @Autowired
    public ProductSnapshotMaintainer(@Lazy ProductVersionService productVersionService,
                                     DistributedLock distributedLock,
                                     @Qualifier("linkDefaultExecutor") ThreadPoolExecutor linkDefaultExecutor) {
        this.productVersionService = productVersionService;
        this.distributedLock = distributedLock;
        this.linkDefaultExecutor = linkDefaultExecutor;
    }

    /**
     * 产品树变更(产品基础信息 / 服务 / 属性 / 命令 / 命令参数 CRUD)→ 事务提交后异步刷新草稿。
     *
     * @param event 产品树变更事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProductModelChanged(ProductModelChangedEvent event) {
        ProductModelChangedSource src = event.getEventSource();
        if (src == null || src.getProductIdentification() == null) {
            return;
        }
        asyncRefreshDraft(src.getProductIdentification(), "productModelChanged");
    }

    /**
     * 异步刷单产品草稿 ── ContextUtil 引用透传保证子线程 @DS 切库正确,分布式锁保证同产品串行。
     * 租户兜底:主线程无 tenantId(事件由后台 / 系统线程发出)时直接跳过并打 error,不静默走默认库,
     * 避免把 A 租户的草稿写到默认库。
     *
     * @param pid     产品标识
     * @param trigger 触发来源(用于日志)
     */
    private void asyncRefreshDraft(String pid, String trigger) {
        if (pid == null || pid.isBlank()) {
            return;
        }
        if (ContextUtil.getTenantId() == null) {
            log.error("[ProductSnapshotMaintainer] skip refresh: no tenantId in ContextUtil"
                + "(可能事件由系统线程发出), trigger={} productIdentification={}", trigger, pid);
            return;
        }
        Map<String, String> ctx = ContextUtil.getLocalMap();
        CompletableFuture.runAsync(() -> {
            try {
                ContextUtil.setLocalMap(ctx);
                // 二次校验子线程上下文(理论上不会丢,留个 fail-fast 防御)
                if (ContextUtil.getTenantId() == null) {
                    log.error("[ProductSnapshotMaintainer] tenantId lost after thread hop, trigger={} productIdentification={}",
                        trigger, pid);
                    return;
                }
                refreshOneDraftLocked(pid, trigger);
            } finally {
                ContextUtil.remove();
            }
        }, linkDefaultExecutor).exceptionally(ex -> {
            log.warn("[ProductSnapshotMaintainer] async refresh failed trigger={} productIdentification={} cause={}",
                trigger, pid, ex.getMessage());
            return null;
        });
    }

    /**
     * 单产品草稿刷新 ── 分布式锁串行化 + 失败重试。锁内执行 upsertDraft(其 @Transactional 在临界区内
     * 完整跑完含提交,锁释放后并发方才能看到刚建的 DRAFT);tryLock 未命中说明同产品已有线程在刷则跳过
     * (幂等,不算失败);抛异常则退避重试最多 {@link #MAX_RETRY} 次,仍失败打 warn 靠下次自愈。
     *
     * @param pid     产品标识
     * @param trigger 触发来源(用于日志)
     */
    private void refreshOneDraftLocked(String pid, String trigger) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                CacheKey lockKey = LinkLockKeyBuilder.forUpsertDraftByProduct(pid);
                LockRunResult<Object> result = distributedLock.tryLockAndRun(
                    lockKey.getKey(),
                    lockKey.getExpire().getSeconds(),
                    TimeUnit.SECONDS,
                    () -> {
                        productVersionService.upsertDraft(pid);
                        return null;
                    });
                if (!result.isLocked()) {
                    log.debug("[ProductSnapshotMaintainer] upsertDraft skipped(并发锁未命中,同产品已有线程在刷) trigger={} productIdentification={}",
                        trigger, pid);
                }
                return; // 成功 or 锁未命中 → 结束(不重试)
            } catch (Exception ex) {
                if (attempt >= MAX_RETRY) {
                    log.warn("[ProductSnapshotMaintainer] upsertDraft failed after {} attempts trigger={} productIdentification={} cause={}",
                        MAX_RETRY, trigger, pid, ex.getMessage());
                    return;
                }
                log.warn("[ProductSnapshotMaintainer] upsertDraft attempt {}/{} failed, will retry. trigger={} productIdentification={} cause={}",
                    attempt, MAX_RETRY, trigger, pid, ex.getMessage());
                sleepQuietly(RETRY_BACKOFF_MS * attempt);
            }
        }
    }

    /**
     * 退避休眠,被中断时恢复中断标志并提前结束。
     *
     * @param millis 休眠毫秒数
     */
    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
