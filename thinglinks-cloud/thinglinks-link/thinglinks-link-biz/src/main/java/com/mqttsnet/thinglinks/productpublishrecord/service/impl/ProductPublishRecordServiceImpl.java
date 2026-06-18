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
import com.mqttsnet.thinglinks.productpublishrecord.vo.result.StrategyResultDTO;
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
    public ProductPublishRecord recordPublish(String productIdentification, String sourceVersion, String targetVersion,
                                              Integer maxRetryCount) {
        return persist(productIdentification, sourceVersion, targetVersion,
            ProductPublishRecordIntentEnum.PUBLISH.getValue(),
            ProductPublishRecordStatusEnum.RUNNING.getValue(), maxRetryCount);
    }

    @Override
    public ProductPublishRecord recordRollback(String productIdentification, String sourceVersion, String targetVersion) {
        // 回滚不提供用户配置入口,maxRetryCount 传 null 走 DB 默认 3
        return persist(productIdentification, sourceVersion, targetVersion,
            ProductPublishRecordIntentEnum.ROLLBACK.getValue(),
            ProductPublishRecordStatusEnum.RUNNING.getValue(), null);
    }

    @Override
    public ProductPublishRecord recordPurge(String productIdentification, String version) {
        // 历史清理不提供用户配置入口,maxRetryCount 传 null 走 DB 默认 3
        return persist(productIdentification, version, version,
            ProductPublishRecordIntentEnum.PURGE_HISTORY.getValue(),
            ProductPublishRecordStatusEnum.RUNNING.getValue(), null);
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
    public void incrementRetryCount(Long recordId) {
        productPublishRecordManager.incrementRetryCount(recordId);
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
    public void attachStrategyResult(Long recordId, StrategyResultDTO result) {
        if (result == null) {
            return;
        }
        Optional.ofNullable(productPublishRecordManager.getById(recordId))
            .ifPresent(record -> {
                record.setCanaryResult(result);
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
                                         String targetVersion, Integer intent, Integer status,
                                         Integer maxRetryCount) {
        // maxRetryCount=null 时不入 builder → MP insert-strategy NOT_NULL 跳过该列 → DB 默认 3 生效
        ProductPublishRecord record = ProductPublishRecord.builder()
            .productIdentification(productIdentification)
            .sourceVersion(sourceVersion)
            .targetVersion(targetVersion)
            .intent(intent)
            .status(status)
            .maxRetryCount(maxRetryCount)
            .startedTime(LocalDateTime.now())
            .build();
        productPublishRecordManager.save(record);
        return record;
    }
}
