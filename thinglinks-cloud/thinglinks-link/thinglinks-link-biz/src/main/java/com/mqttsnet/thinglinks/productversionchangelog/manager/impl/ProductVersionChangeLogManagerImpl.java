package com.mqttsnet.thinglinks.productversionchangelog.manager.impl;

import java.util.List;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.thinglinks.productversionchangelog.entity.ProductVersionChangeLog;
import com.mqttsnet.thinglinks.productversionchangelog.manager.ProductVersionChangeLogManager;
import com.mqttsnet.thinglinks.productversionchangelog.mapper.ProductVersionChangeLogMapper;
import com.mqttsnet.thinglinks.productversionchangelog.vo.query.ProductVersionChangeLogPageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 产品物模型版本变更日志通用业务实现。
 *
 * @author mqttsnet
 * @see ProductVersionChangeLogManager
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductVersionChangeLogManagerImpl
    extends SuperManagerImpl<ProductVersionChangeLogMapper, ProductVersionChangeLog>
    implements ProductVersionChangeLogManager {

    private final ProductVersionChangeLogMapper productVersionChangeLogMapper;

    @Override
    public IPage<ProductVersionChangeLog> getPage(PageParams<ProductVersionChangeLogPageQuery> params) {
        IPage<ProductVersionChangeLog> page = params.buildPage(ProductVersionChangeLog.class);
        ProductVersionChangeLogPageQuery model = params.getModel();

        LbQueryWrap<ProductVersionChangeLog> wrap = Wraps.lbQ();
        wrap.eq(StrUtil.isNotBlank(model.getProductIdentification()),
                ProductVersionChangeLog::getProductIdentification, model.getProductIdentification())
            .eq(StrUtil.isNotBlank(model.getVersionNo()),
                ProductVersionChangeLog::getVersionNo, model.getVersionNo())
            .eq(model.getChangeType() != null,
                ProductVersionChangeLog::getChangeType, model.getChangeType())
            .orderByDesc(ProductVersionChangeLog::getCreatedTime);

        return productVersionChangeLogMapper.selectPage(page, wrap);
    }

    @Override
    public List<ProductVersionChangeLog> listByProductIdentification(String productIdentification) {
        LbQueryWrap<ProductVersionChangeLog> wrap = Wraps.lbQ();
        wrap.eq(ProductVersionChangeLog::getProductIdentification, productIdentification)
            .orderByDesc(ProductVersionChangeLog::getCreatedTime);
        return productVersionChangeLogMapper.selectList(wrap);
    }
}
