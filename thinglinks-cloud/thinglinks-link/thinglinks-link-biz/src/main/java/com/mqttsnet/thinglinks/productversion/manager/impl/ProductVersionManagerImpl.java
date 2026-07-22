package com.mqttsnet.thinglinks.productversion.manager.impl;

import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;
import com.mqttsnet.thinglinks.productversion.manager.ProductVersionManager;
import com.mqttsnet.thinglinks.productversion.mapper.ProductVersionMapper;
import com.mqttsnet.thinglinks.productversion.vo.query.ProductVersionPageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 产品物模型版本快照通用业务实现。
 *
 * @author mqttsnet
 * @see ProductVersionManager
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductVersionManagerImpl extends SuperManagerImpl<ProductVersionMapper, ProductVersion>
    implements ProductVersionManager {

    private final ProductVersionMapper productVersionMapper;

    @Override
    public IPage<ProductVersion> getPage(PageParams<ProductVersionPageQuery> params) {
        IPage<ProductVersion> page = params.buildPage(ProductVersion.class);
        ProductVersionPageQuery model = params.getModel();

        LbQueryWrap<ProductVersion> wrap = Wraps.lbQ();
        wrap.eq(StrUtil.isNotBlank(model.getProductIdentification()),
                ProductVersion::getProductIdentification, model.getProductIdentification())
            .eq(StrUtil.isNotBlank(model.getVersionNo()),
                ProductVersion::getVersionNo, model.getVersionNo())
            .eq(model.getVersionStatus() != null,
                ProductVersion::getVersionStatus, model.getVersionStatus())
            .eq(model.getPublishStrategy() != null,
                ProductVersion::getPublishStrategy, model.getPublishStrategy())
            .orderByDesc(ProductVersion::getCreatedTime);

        return productVersionMapper.selectPage(page, wrap);
    }

    @Override
    public Optional<ProductVersion> findByProductIdentificationAndVersionNo(String productIdentification, String versionNo) {
        if (StrUtil.isBlank(productIdentification) || StrUtil.isBlank(versionNo)) {
            return Optional.empty();
        }
        LbQueryWrap<ProductVersion> wrap = Wraps.lbQ();
        wrap.eq(ProductVersion::getProductIdentification, productIdentification)
            .eq(ProductVersion::getVersionNo, versionNo);
        return Optional.ofNullable(productVersionMapper.selectOne(wrap));
    }

    @Override
    public List<ProductVersion> listByProductIdentification(String productIdentification) {
        LbQueryWrap<ProductVersion> wrap = Wraps.lbQ();
        wrap.eq(ProductVersion::getProductIdentification, productIdentification)
            .orderByDesc(ProductVersion::getCreatedTime);
        return productVersionMapper.selectList(wrap);
    }

    @Override
    public List<ProductVersion> listByProductIdentificationAndStatus(String productIdentification, Integer versionStatus) {
        LbQueryWrap<ProductVersion> wrap = Wraps.lbQ();
        wrap.eq(ProductVersion::getProductIdentification, productIdentification)
            .eq(versionStatus != null, ProductVersion::getVersionStatus, versionStatus)
            .orderByDesc(ProductVersion::getCreatedTime);
        return productVersionMapper.selectList(wrap);
    }

    @Override
    public Optional<ProductVersion> findDraft(String productIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            return Optional.empty();
        }
        LbQueryWrap<ProductVersion> wrap = Wraps.lbQ();
        wrap.eq(ProductVersion::getProductIdentification, productIdentification)
            .eq(ProductVersion::getVersionStatus,
                com.mqttsnet.thinglinks.productversion.enumeration.ProductVersionStatusEnum.DRAFT.getValue())
            .orderByDesc(ProductVersion::getCreatedTime)
            .last("LIMIT 1");
        return Optional.ofNullable(productVersionMapper.selectOne(wrap));
    }

    @Override
    public Long countByVersionStatus(Integer versionStatus) {
        if (versionStatus == null) {
            return 0L;
        }
        LbQueryWrap<ProductVersion> wrap = Wraps.lbQ();
        wrap.eq(ProductVersion::getVersionStatus, versionStatus);
        return productVersionMapper.selectCount(wrap);
    }
}
