package com.mqttsnet.thinglinks.productversion.service;

import java.util.Optional;

import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;

/**
 * 产品版本只读查询 Service(leaf 业务层)。独立于 {@link ProductVersionService},仅持有 ProductVersionManager,
 * 零下游 Service 依赖使类图天然为 DAG,从根本规避
 * ProductVersionService ↔ ProductService ↔ LinkCacheDataHelper ↔ ProductModelCacheService 的构造期循环依赖
 * (以前用 @Lazy 掩盖)。专供 cache helper / 跨域 Service 反向查询版本快照。
 *
 * @author mqttsnet
 * @since 2026-05-28
 */
public interface ProductVersionQueryService {

    /**
     * 按 (productIdentification, versionNo) 反查产品版本快照。
     */
    Optional<ProductVersion> findByProductIdentificationAndVersionNo(String productIdentification, String versionNo);
}
