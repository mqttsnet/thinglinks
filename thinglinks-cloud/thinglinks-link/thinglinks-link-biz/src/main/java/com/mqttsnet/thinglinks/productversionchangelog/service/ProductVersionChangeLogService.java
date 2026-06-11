package com.mqttsnet.thinglinks.productversionchangelog.service;

import java.util.List;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.productversionchangelog.entity.ProductVersionChangeLog;
import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductChangeTargetTypeEnum;
import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductVersionChangeTypeEnum;

/**
 * 产品物模型版本变更日志业务接口。
 *
 * @author mqttsnet
 * @see ProductVersionChangeLog
 */
public interface ProductVersionChangeLogService extends SuperService<Long, ProductVersionChangeLog> {

    /**
     * 追加一条变更日志(append-only)。
     *
     * <p>由 ProductVersionService.upsertDraft 刷新草稿后调用:diff 旧/新草稿快照,
     * 有变更则记一行。productIdentification 为空时静默跳过。</p>
     *
     * @param productIdentification 产品标识
     * @param versionNo             本批变更归属版本序号
     * @param changeType            变更类型(新增 / 编辑 / 删除)
     * @param targetType            变更维度(产品信息 / 服务 / 属性 / 命令)
     * @param changeSummary         变更摘要(人类可读)
     * @param changeDetailJson      字段级变更明细 JSON(可空)
     */
    void record(String productIdentification, String versionNo, ProductVersionChangeTypeEnum changeType,
                ProductChangeTargetTypeEnum targetType, String changeSummary, String changeDetailJson);

    /**
     * 按产品标识查询全部变更日志(created_time 倒序)。
     *
     * @param productIdentification 产品标识
     * @return 变更日志列表
     */
    List<ProductVersionChangeLog> listByProductIdentification(String productIdentification);
}
