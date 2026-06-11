package com.mqttsnet.thinglinks.productversionchangelog.listener;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.thinglinks.product.event.ProductModelChangedEvent;
import com.mqttsnet.thinglinks.product.event.source.ProductModelChangedSource;
import com.mqttsnet.thinglinks.productversion.diff.EntityFieldDiffer;
import com.mqttsnet.thinglinks.productversion.service.ProductVersionService;
import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductVersionChangeTypeEnum;
import com.mqttsnet.thinglinks.productversionchangelog.service.ProductVersionChangeLogService;
import com.mqttsnet.thinglinks.productversionchangelog.vo.FieldChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 产品变更记录监听器 —— 消费 {@link ProductModelChangedEvent},一次动作落一行变更记录。AFTER_COMMIT 触发:
 * 回滚的 CRUD 不留痕,每事件一条精确记录不合并(批量导入 N 次 = N 行)。异步丢具名 linkDefaultExecutor 把 audit
 * insert 从请求 critical path 挪走,主线程 ctx 引用透传给异步线程。记录归属当前草稿版本,发布后随版本固化。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
public class ProductChangeLogListener {

    private final ProductVersionService productVersionService;
    private final ProductVersionChangeLogService productVersionChangeLogService;
    private final EntityFieldDiffer entityFieldDiffer;
    private final ThreadPoolExecutor linkDefaultExecutor;

    @Autowired
    public ProductChangeLogListener(@Lazy ProductVersionService productVersionService,
                                    ProductVersionChangeLogService productVersionChangeLogService,
                                    EntityFieldDiffer entityFieldDiffer,
                                    @Qualifier("linkDefaultExecutor") ThreadPoolExecutor linkDefaultExecutor) {
        this.productVersionService = productVersionService;
        this.productVersionChangeLogService = productVersionChangeLogService;
        this.entityFieldDiffer = entityFieldDiffer;
        this.linkDefaultExecutor = linkDefaultExecutor;
    }

    /**
     * 物模型变更 → 落一行变更记录。异步在 linkDefaultExecutor 跑,失败仅打 warn 不影响触发它的 CRUD 主流程。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProductModelChanged(ProductModelChangedEvent event) {
        ProductModelChangedSource src = event.getEventSource();
        if (src == null || src.getProductIdentification() == null) {
            return;
        }
        // 多租户隔离 fail-fast:DsConstant.BASE_TENANT = "#thread.thinglinks_base" 走 SPEL 取
        // ThreadLocal 数据源名;若 tenantId 缺失,SPEL 返 null → dynamic-datasource 静默
        // fallback 到 primary "0" 默认库 → 跨租户数据串味。这里早返避免此风险。
        if (ContextUtil.getTenantId() == null) {
            log.error("[ProductChangeLog] skip record: no tenantId in ContextUtil"
                + "(可能事件由系统线程发出), productIdentification={}", src.getProductIdentification());
            return;
        }
        Map<String, String> ctx = ContextUtil.getLocalMap();
        CompletableFuture.runAsync(() -> {
            try {
                ContextUtil.setLocalMap(ctx);
                doRecord(src);
            } finally {
                ContextUtil.remove();
            }
        }, linkDefaultExecutor).exceptionally(ex -> {
            log.warn("[ProductChangeLog] record failed productIdentification={} cause={}", src.getProductIdentification(), ex.getMessage());
            return null;
        });
    }

    /**
     * 实际落库逻辑 ── 抽出来便于单测 + lambda 内体积小。
     */
    private void doRecord(ProductModelChangedSource src) {
        // 1. 当前草稿版本(变更归属版本)
        String version = productVersionService.resolveDraftVersion(src.getProductIdentification());
        // 2. 字段级明细 —— 直接比对事件自带的 before / after
        List<FieldChange> fields = entityFieldDiffer.diff(src.getBefore(), src.getAfter());
        // 3. 编辑但无字段变化 → 不留痕(no-op 不记)
        boolean isNoopUpdate = Optional.ofNullable(src.getChangeType())
            .filter(ProductVersionChangeTypeEnum.UPDATE::equals)
            .isPresent() && fields.isEmpty();
        if (isNoopUpdate) {
            return;
        }
        // 4. 落一行
        productVersionChangeLogService.record(
            src.getProductIdentification(), version,
            src.getChangeType(), src.getTargetType(),
            src.getChangeSummary(), JsonUtil.toJson(fields));
    }
}
