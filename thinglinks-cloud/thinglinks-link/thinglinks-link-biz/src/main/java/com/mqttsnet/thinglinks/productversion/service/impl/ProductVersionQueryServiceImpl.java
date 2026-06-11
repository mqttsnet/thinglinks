package com.mqttsnet.thinglinks.productversion.service.impl;

import java.util.Optional;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;
import com.mqttsnet.thinglinks.productversion.manager.ProductVersionManager;
import com.mqttsnet.thinglinks.productversion.service.ProductVersionQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 产品版本只读查询 Service 实现。
 *
 * <p>仅持有 {@link ProductVersionManager},零下游 Service 依赖,类图天然为 DAG。</p>
 *
 * @author mqttsnet
 * @since 2026-05-28
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductVersionQueryServiceImpl implements ProductVersionQueryService {

    private final ProductVersionManager productVersionManager;

    @Override
    public Optional<ProductVersion> findByProductIdentificationAndVersionNo(String productIdentification, String versionNo) {
        return productVersionManager.findByProductIdentificationAndVersionNo(productIdentification, versionNo);
    }
}
