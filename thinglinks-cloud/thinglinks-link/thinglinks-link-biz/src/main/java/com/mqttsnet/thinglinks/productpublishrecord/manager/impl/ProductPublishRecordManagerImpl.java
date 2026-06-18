package com.mqttsnet.thinglinks.productpublishrecord.manager.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.thinglinks.productpublishrecord.entity.ProductPublishRecord;
import com.mqttsnet.thinglinks.productpublishrecord.manager.ProductPublishRecordManager;
import com.mqttsnet.thinglinks.productpublishrecord.mapper.ProductPublishRecordMapper;
import com.mqttsnet.thinglinks.productpublishrecord.vo.query.ProductPublishRecordPageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品发布记录通用业务实现。
 *
 * @author mqttsnet
 * @see ProductPublishRecordManager
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductPublishRecordManagerImpl extends SuperManagerImpl<ProductPublishRecordMapper, ProductPublishRecord>
        implements ProductPublishRecordManager {

    private final ProductPublishRecordMapper productPublishRecordMapper;

    @Override
    public IPage<ProductPublishRecord> getPage(PageParams<ProductPublishRecordPageQuery> params) {
        IPage<ProductPublishRecord> page = params.buildPage(ProductPublishRecord.class);
        ProductPublishRecordPageQuery model = params.getModel();

        LbQueryWrap<ProductPublishRecord> wrap = Wraps.lbQ();
        wrap.eq(StrUtil.isNotBlank(model.getProductIdentification()),
                        ProductPublishRecord::getProductIdentification, model.getProductIdentification())
                .eq(StrUtil.isNotBlank(model.getTargetVersion()),
                        ProductPublishRecord::getTargetVersion, model.getTargetVersion())
                .eq(model.getIntent() != null, ProductPublishRecord::getIntent, model.getIntent())
                .eq(model.getStatus() != null, ProductPublishRecord::getStatus, model.getStatus())
                .orderByDesc(ProductPublishRecord::getCreatedTime);

        return productPublishRecordMapper.selectPage(page, wrap);
    }

    @Override
    public List<ProductPublishRecord> listByProductIdentification(String productIdentification) {
        LbQueryWrap<ProductPublishRecord> wrap = Wraps.lbQ();
        wrap.eq(ProductPublishRecord::getProductIdentification, productIdentification)
                .orderByDesc(ProductPublishRecord::getCreatedTime);
        return productPublishRecordMapper.selectList(wrap);
    }

    @Override
    public List<ProductPublishRecord> listByStatusSince(Integer status, LocalDateTime sinceTime, int limit) {
        if (status == null || sinceTime == null) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 500));
        LbQueryWrap<ProductPublishRecord> wrap = Wraps.lbQ();
        wrap.eq(ProductPublishRecord::getStatus, status)
                .ge(ProductPublishRecord::getCreatedTime, sinceTime)
                .orderByAsc(ProductPublishRecord::getCreatedTime)
                .last("LIMIT " + safeLimit);
        return productPublishRecordMapper.selectList(wrap);
    }

    @Override
    public void incrementRetryCount(Long recordId) {
        if (recordId == null) {
            return;
        }
        productPublishRecordMapper.update(null, Wrappers.<ProductPublishRecord>lambdaUpdate()
                .setSql("retry_count = retry_count + 1")
                .eq(ProductPublishRecord::getId, recordId));
    }

    @Override
    public Long countSuccessfulPublishesInLastDays(int sinceDays) {
        int safeWindow = Math.max(sinceDays, 1);
        LocalDateTime since = LocalDateTime.now().minusDays(safeWindow);
        LbQueryWrap<ProductPublishRecord> wrap = Wraps.lbQ();
        // intent=PUBLISH(0) + status=SUCCESS(1) + created_time >= since
        wrap.eq(ProductPublishRecord::getIntent, 0)
                .eq(ProductPublishRecord::getStatus, 1)
                .ge(ProductPublishRecord::getCreatedTime, since);
        return productPublishRecordMapper.selectCount(wrap);
    }
}
