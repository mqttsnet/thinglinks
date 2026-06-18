package com.mqttsnet.thinglinks.cache.product;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.cache.CacheSuperAbstract;
import com.mqttsnet.thinglinks.cache.vo.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.common.cache.link.product.ProductModelCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.product.service.ProductQueryService;
import com.mqttsnet.thinglinks.product.vo.query.ProductPageQuery;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.productversion.converter.ProductSnapshotConverter;
import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;
import com.mqttsnet.thinglinks.productversion.service.ProductVersionQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 产品物模型缓存服务,按 (productIdentification, versionNo) 双维度切分。
 * 主动刷新只覆盖产品当前激活版本(activeVersionNo),历史版本不预热,首次按版本读取时 read-through 回源 ──
 * 版本快照不可变 + TTL 7 天,首次回源后稳定命中,避免预热"全部产品 × 全部历史版本 × 全部租户"拉高 Redis 写 QPS。
 *
 * @author mqttsnet
 */
@DS(DsConstant.BASE_TENANT)
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductModelCacheService extends CacheSuperAbstract {

    private final CachePlusOps cachePlusOps;
    private final ProductQueryService productQueryService;
    /** 走快照回源依赖 ── 持有 leaf QueryService(零下游 Service 依赖),类图天然 DAG,无需 @Lazy。 */
    private final ProductVersionQueryService productVersionQueryService;
    private final ProductSnapshotConverter productSnapshotConverter;

    /**
     * 按 (productIdentification, versionNo) 加载特定版本快照 ── read-through loader,供
     * LinkCacheDataHelper#resolveProductModelByVersionNo 回源调用。严格按设备绑定的版本反查 product_version 表,
     * 即使产品已发新版本,老版本设备仍取老版本快照(灰度路由的关键)。参数空 / 快照不存在 / 反序列化失败返 null;
     * 写缓存由 CachePlusUtil.getOrLoad 统一负责,tenantId 从 {@link ContextUtil} 取与 helper 上下文一致。
     *
     * @param productIdentification 产品标识
     * @param versionNo             版本序号(必填,空值不走本方法)
     * @return 物模型 VO;失败返 null
     */
    public ProductModelCacheVO loadProductModelFromDbByVersionNo(String productIdentification, String versionNo) {
        if (StrUtil.hasBlank(productIdentification, versionNo)) {
            return null;
        }
        Long tenantId = ContextUtil.getTenantId();
        try {
            ProductModelCacheVO vo = productVersionQueryService
                .findByProductIdentificationAndVersionNo(productIdentification, versionNo)
                .map(ProductVersion::getProductSnapshotJson)
                .flatMap(productSnapshotConverter::deserialize)
                .map(productSnapshotConverter::toModelCacheVO)
                .orElse(null);
            if (vo == null) {
                log.warn("[product-model-by-version] snapshot not found productIdentification={} versionNo={} tenantId={}",
                    productIdentification, versionNo, tenantId);
                return null;
            }
            vo.setTenantId(tenantId);
            log.info("[product-model-by-version] resolved productIdentification={} versionNo={} tenantId={}",
                productIdentification, versionNo, tenantId);
            return vo;
        } catch (Exception e) {
            log.error("[product-model-by-version] load failed productIdentification={} versionNo={}",
                productIdentification, versionNo, e);
            return null;
        }
    }

    /**
     * 全量刷新指定租户所有产品的当前激活版本物模型缓存 ── LinkJobHandler 调度入口。
     * 遍历该租户全部产品取各自 activeVersionNo 主动预热,历史版本不预热,首次按版本读取时 read-through 回源。
     *
     * @param tenantId 租户 ID,不能为空
     */
    public void refreshAllProductModelCacheForTenant(Long tenantId) {
        if (tenantId == null) {
            return;
        }
        long startTime = System.currentTimeMillis();
        AtomicInteger totalProducts = new AtomicInteger();
        AtomicInteger totalSuccess = new AtomicInteger();
        AtomicInteger totalFail = new AtomicInteger();
        int totalProductsCount = productQueryService.findProductTotal().intValue();
        int totalPages = (int) Math.ceil((double) totalProductsCount / PAGE_SIZE);
        log.info("[product-model-job] start tenantId={} totalProducts={} pages={}", tenantId, totalProductsCount, totalPages);

        IntStream.rangeClosed(1, totalPages).sequential().forEach(page -> {
            List<ProductResultVO> products = fetchProductPage(page);
            products.forEach(product -> {
                totalProducts.incrementAndGet();
                if (refreshProductModelCache(product.getProductIdentification())) {
                    totalSuccess.incrementAndGet();
                } else {
                    totalFail.incrementAndGet();
                }
            });
        });

        log.info("[product-model-job] done tenantId={} products={} (success={} fail={}) cost={}ms",
            tenantId, totalProducts.get(), totalSuccess.get(), totalFail.get(),
            System.currentTimeMillis() - startTime);
    }

    /**
     * 分页查产品(供全量刷新用)。
     *
     * @param currentPage 当前页码(从 1 起)
     * @return 当前页产品列表;页码非法返空列表
     */
    private List<ProductResultVO> fetchProductPage(int currentPage) {
        if (currentPage < 1) {
            return Collections.emptyList();
        }
        PageParams<ProductPageQuery> params = new PageParams<>(currentPage, PAGE_SIZE);
        params.setModel(ProductPageQuery.builder().build());
        return Optional.ofNullable(productQueryService.getPage(params))
            .map(IPage::getRecords)
            .orElse(Collections.emptyList());
    }

    /**
     * 事件驱动 / 调度任务触发的单产品物模型缓存主动刷新 ── 取产品当前激活版本 activeVersionNo 反查快照写入缓存;
     * 灰度态(previousFullVersionNo 非空)额外预热稳定版,因灰度期稳定设备 + 新设备都绑稳定版,一并暖缓存避免走
     * read-through。更早的历史版本(已回滚 / 被彻底切走)由 read-through 按需回源,不主动预热。
     *
     * @param productIdentification 产品标识
     * @return 当前激活版本是否刷新成功(产品不存在 / 当前未激活任何版本 / 异常时返 false;稳定版为尽力而为不影响返回值)
     */
    public boolean refreshProductModelCache(String productIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            return false;
        }
        try {
            Long tenantId = ContextUtil.getTenantId();
            ProductResultVO product = productQueryService.findOneByProductIdentification(productIdentification);
            if (product == null) {
                log.warn("[product-model-refresh] product not found productIdentification={}", productIdentification);
                return false;
            }
            String activeVersionNo = product.getActiveVersionNo();
            if (StrUtil.isBlank(activeVersionNo)) {
                log.info("[product-model-refresh] skipped: product not published yet productIdentification={}",
                    productIdentification);
                return false;
            }
            boolean ok = warmModelVersion(productIdentification, activeVersionNo, tenantId);
            // 灰度态额外预热稳定版(previousFullVersionNo):尽力而为,失败不影响当前激活版本的刷新结果
            String previousFullVersionNo = product.getPreviousFullVersionNo();
            if (StrUtil.isNotBlank(previousFullVersionNo) && !previousFullVersionNo.equals(activeVersionNo)) {
                warmModelVersion(productIdentification, previousFullVersionNo, tenantId);
            }
            return ok;
        } catch (Exception e) {
            log.error("[product-model-refresh] failed productIdentification={}", productIdentification, e);
            return false;
        }
    }

    /**
     * 预热单个版本的物模型缓存:反查版本快照写入缓存。
     *
     * @param productIdentification 产品标识
     * @param versionNo             版本序号
     * @param tenantId              租户 ID(日志用)
     * @return 是否成功(快照反查失败返 false)
     */
    private boolean warmModelVersion(String productIdentification, String versionNo, Long tenantId) {
        ProductModelCacheVO vo = loadProductModelFromDbByVersionNo(productIdentification, versionNo);
        if (vo == null) {
            log.warn("[product-model-refresh] snapshot resolution failed productIdentification={} versionNo={}",
                productIdentification, versionNo);
            return false;
        }
        CacheKey cacheKey = ProductModelCacheKeyBuilder.build(productIdentification, versionNo);
        cachePlusOps.del(cacheKey);
        cachePlusOps.set(cacheKey, vo);
        log.info("[product-model-refresh] cache refreshed productIdentification={} versionNo={} tenantId={}",
            productIdentification, versionNo, tenantId);
        return true;
    }
}
