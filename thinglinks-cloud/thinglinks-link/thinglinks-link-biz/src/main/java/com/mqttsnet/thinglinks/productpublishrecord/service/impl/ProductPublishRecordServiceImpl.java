package com.mqttsnet.thinglinks.productpublishrecord.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.productpublishrecord.entity.ProductPublishRecord;
import com.mqttsnet.thinglinks.productpublishrecord.enumeration.ProductPublishRecordIntentEnum;
import com.mqttsnet.thinglinks.productpublishrecord.enumeration.ProductPublishRecordStatusEnum;
import com.mqttsnet.thinglinks.productpublishrecord.manager.ProductPublishRecordManager;
import com.mqttsnet.thinglinks.productpublishrecord.service.ProductPublishRecordService;
import com.mqttsnet.thinglinks.productpublishrecord.vo.ddl.PublishDdlItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 产品发布记录业务实现。
 *
 * @author mqttsnet
 * @see ProductPublishRecordService
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductPublishRecordServiceImpl
    extends SuperServiceImpl<ProductPublishRecordManager, Long, ProductPublishRecord>
    implements ProductPublishRecordService {

    private final ProductPublishRecordManager productPublishRecordManager;

    @Override
    public ProductPublishRecord recordPublish(String productIdentification, String sourceVersion, String targetVersion) {
        return persist(productIdentification, sourceVersion, targetVersion,
            ProductPublishRecordIntentEnum.PUBLISH.getValue(),
            ProductPublishRecordStatusEnum.RUNNING.getValue());
    }

    @Override
    public ProductPublishRecord recordRollback(String productIdentification, String sourceVersion, String targetVersion) {
        return persist(productIdentification, sourceVersion, targetVersion,
            ProductPublishRecordIntentEnum.ROLLBACK.getValue(),
            ProductPublishRecordStatusEnum.RUNNING.getValue());
    }

    @Override
    public ProductPublishRecord recordPurge(String productIdentification, String version) {
        return persist(productIdentification, version, version,
            ProductPublishRecordIntentEnum.PURGE_HISTORY.getValue(),
            ProductPublishRecordStatusEnum.RUNNING.getValue());
    }

    @Override
    public void markFailed(Long recordId, String failedReason) {
        Optional.ofNullable(productPublishRecordManager.getById(recordId))
            .ifPresent(record -> {
                record.setStatus(ProductPublishRecordStatusEnum.FAILED.getValue());
                record.setFailedReason(failedReason);
                record.setFinishedTime(LocalDateTime.now());
                productPublishRecordManager.updateById(record);
            });
    }

    @Override
    public void markSuccess(Long recordId) {
        Optional.ofNullable(productPublishRecordManager.getById(recordId))
            .ifPresent(record -> {
                record.setStatus(ProductPublishRecordStatusEnum.SUCCESS.getValue());
                record.setFinishedTime(LocalDateTime.now());
                productPublishRecordManager.updateById(record);
            });
    }

    @Override
    public List<ProductPublishRecord> listByStatusSince(Integer status, LocalDateTime sinceTime, int limit) {
        return productPublishRecordManager.listByStatusSince(status, sinceTime, limit);
    }

    @Override
    public Long countSuccessfulPublishesInLastDays(int sinceDays) {
        return productPublishRecordManager.countSuccessfulPublishesInLastDays(sinceDays);
    }

    @Override
    public void attachDdlItems(Long recordId, List<PublishDdlItemVO> items) {
        Optional.ofNullable(productPublishRecordManager.getById(recordId))
            .ifPresent(record -> {
                record.setDdlItems(items);
                productPublishRecordManager.updateById(record);
            });
    }

    @Override
    public void attachRemark(Long recordId, String remark) {
        Optional.ofNullable(productPublishRecordManager.getById(recordId))
            .ifPresent(record -> {
                record.setRemark(remark);
                productPublishRecordManager.updateById(record);
            });
    }

    private ProductPublishRecord persist(String productIdentification, String sourceVersion,
                                         String targetVersion, Integer intent, Integer status) {
        ProductPublishRecord record = ProductPublishRecord.builder()
            .productIdentification(productIdentification)
            .sourceVersion(sourceVersion)
            .targetVersion(targetVersion)
            .intent(intent)
            .status(status)
            .startedTime(LocalDateTime.now())
            .build();
        productPublishRecordManager.save(record);
        return record;
    }
}
