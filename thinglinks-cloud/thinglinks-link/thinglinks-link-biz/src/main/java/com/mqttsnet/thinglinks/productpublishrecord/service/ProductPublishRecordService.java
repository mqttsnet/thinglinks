package com.mqttsnet.thinglinks.productpublishrecord.service;

import java.time.LocalDateTime;
import java.util.List;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.productpublishrecord.entity.ProductPublishRecord;
import com.mqttsnet.thinglinks.productpublishrecord.vo.ddl.PublishDdlItemVO;
import com.mqttsnet.thinglinks.productpublishrecord.vo.result.StrategyResultDTO;

/**
 * 产品发布记录业务接口。
 *
 * @author mqttsnet
 * @see ProductPublishRecord
 */
public interface ProductPublishRecordService extends SuperService<Long, ProductPublishRecord> {

    /**
     * 写入一条 PUBLISH 记录(默认状态 SUCCESS,失败由 caller 调 markFailed 修改)。sourceVersion 首发为 null。
     *
     * @param productIdentification 产品标识
     * @param sourceVersion         发布前生效版本(首发为 null)
     * @param targetVersion         本次发布的目标版本
     * @param maxRetryCount         最大兜底重试次数(用户可配,null 走 DB 默认 3)
     * @return 写入的发布记录
     */
    ProductPublishRecord recordPublish(String productIdentification, String sourceVersion, String targetVersion,
                                       Integer maxRetryCount);

    /**
     * 写入一条 ROLLBACK 记录,状态 SUCCESS。sourceVersion=回滚前生效版本,targetVersion=回滚到的版本。
     *
     * @param productIdentification 产品标识
     * @param sourceVersion         回滚前生效版本
     * @param targetVersion         回滚到的版本
     * @return 写入的回滚记录
     */
    ProductPublishRecord recordRollback(String productIdentification, String sourceVersion, String targetVersion);

    /**
     * 写入一条 PURGE_HISTORY 记录,状态 SUCCESS(sourceVersion = targetVersion)。
     *
     * @param productIdentification 产品标识
     * @param version               被清理的版本号
     * @return 写入的清理记录
     */
    ProductPublishRecord recordPurge(String productIdentification, String version);

    /**
     * 把一条记录标记为失败。
     *
     * @param recordId     记录 ID
     * @param failedReason 失败原因
     */
    void markFailed(Long recordId, String failedReason);

    /**
     * 把一条记录标记为成功(状态 + 结束时间)。
     *
     * @param recordId 记录 ID
     */
    void markSuccess(Long recordId);

    /**
     * 原子自增指定记录的 Job 兜底重试次数(retry_count + 1)。供 orchestrator 重试前计数,达上限不再重跑。
     *
     * @param recordId 记录 ID
     */
    void incrementRetryCount(Long recordId);

    /**
     * 写入 / 覆盖记录的备注信息(回滚等无 DDL 流水场景把改绑设备数 / from-to 版本号写到 remark 供前端展示)。
     *
     * @param recordId 记录 ID
     * @param remark   备注文案(全量覆盖,而非追加)
     */
    void attachRemark(Long recordId, String remark);

    /**
     * 写入 / 覆盖 DDL 执行明细列表(全量覆盖)。{@link com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}
     * 自动把 list 序列化到 ddl_summary JSON 列,调用方无需手动 JSON.toString。
     *
     * @param recordId 记录 ID
     * @param items    DDL 执行明细列表
     */
    void attachDdlItems(Long recordId, List<PublishDdlItemVO> items);

    /**
     * 回写策略执行结果快照到 canary_result_json(发布动作完成后由编排器调用,markSuccess 前)。
     *
     * @param recordId 发布记录 ID
     * @param result   策略执行结果(全量/灰度/影子按策略填不同字段);null 时跳过
     */
    void attachStrategyResult(Long recordId, StrategyResultDTO result);

    /**
     * 查指定状态(0=RUNNING / 1=SUCCESS / 2=FAILED)+ 时间窗内的记录,供兜底 retry job 扫卡住的发布。返回老的优先(createdTime ASC)。
     *
     * @param status    记录状态(0=RUNNING / 1=SUCCESS / 2=FAILED)
     * @param sinceTime 时间窗起点
     * @param limit     最大返回条数
     * @return 符合条件的发布记录列表(createdTime 升序)
     */
    List<ProductPublishRecord> listByStatusSince(Integer status, LocalDateTime sinceTime, int limit);

    /**
     * 统计近 sinceDays 天内成功发布的记录数(intent=PUBLISH=0 + status=SUCCESS=1),供总览页卡片用。
     * 跨域调用方应走 Service 层确保 @DS(BASE_TENANT) 切租户库,不要直接调 Manager。
     *
     * @param sinceDays 统计的最近天数
     * @return 近 sinceDays 天内成功发布的记录数
     */
    Long countSuccessfulPublishesInLastDays(int sinceDays);
}
