package com.mqttsnet.thinglinks.productpublishrecord.manager;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.thinglinks.productpublishrecord.entity.ProductPublishRecord;
import com.mqttsnet.thinglinks.productpublishrecord.vo.query.ProductPublishRecordPageQuery;

/**
 * 产品发布记录通用业务接口。
 *
 * @author mqttsnet
 * @see ProductPublishRecord
 */
public interface ProductPublishRecordManager extends SuperManager<ProductPublishRecord> {

    /**
     * 分页查询发布记录。
     *
     * @param params 分页参数
     * @return {@link IPage} 分页结果
     */
    IPage<ProductPublishRecord> getPage(PageParams<ProductPublishRecordPageQuery> params);

    /**
     * 按产品标识查询发布记录(按 created_time 倒序)。
     *
     * @param productIdentification 产品标识
     * @return {@link List} 发布记录列表
     */
    List<ProductPublishRecord> listByProductIdentification(String productIdentification);

    /**
     * 统计指定时间窗内成功发布的记录数(intent=PUBLISH=0 + status=SUCCESS=1)。
     *
     * <p>用于总览页"近 N 天发布次数"指标卡。</p>
     *
     * @param sinceDays 时间窗(N 天前到现在,例如 7)
     * @return 发布次数
     */
    Long countSuccessfulPublishesInLastDays(int sinceDays);

    /**
     * 查指定状态 + 时间窗内的发布记录(供兜底 retry job 扫描 status=RUNNING)。
     *
     * <p>限制:只拉 since 之后创建的,避免远古无效记录被反复重试。</p>
     *
     * @param status    状态值(0=RUNNING / 1=SUCCESS / 2=FAILED)
     * @param sinceTime 时间下界(createdTime &gt;= since)
     * @param limit     最多返回多少条(避免 job 一次拖太多)
     * @return 按 createdTime ASC(老的优先重试)的记录列表
     */
    List<ProductPublishRecord> listByStatusSince(Integer status, LocalDateTime sinceTime, int limit);

    /**
     * 原子自增指定记录的 retry_count(retry_count = retry_count + 1)。
     * 用 setSql 而非 updateById 读改写:避免全字段回写覆盖并发(原始 async 可能正在 markSuccess),且天生原子。
     *
     * @param recordId 记录 ID
     */
    void incrementRetryCount(Long recordId);
}
