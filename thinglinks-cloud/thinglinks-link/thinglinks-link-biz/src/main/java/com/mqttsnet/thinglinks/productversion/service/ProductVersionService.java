package com.mqttsnet.thinglinks.productversion.service;

import java.util.List;
import java.util.Optional;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.productpublishrecord.entity.ProductPublishRecord;
import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;
import com.mqttsnet.thinglinks.productversion.vo.diff.ProductVersionDiffVO;
import com.mqttsnet.thinglinks.productversion.vo.result.ProductVersionStatisticsResultVO;
import com.mqttsnet.thinglinks.productversion.vo.save.ProductVersionPublishVO;
import com.mqttsnet.thinglinks.productversion.vo.save.ProductVersionPurgeVO;
import com.mqttsnet.thinglinks.productversion.vo.save.ProductVersionRollbackVO;

/**
 * 产品物模型版本业务接口。
 *
 * @author mqttsnet
 * @see ProductVersion
 */
public interface ProductVersionService extends SuperService<Long, ProductVersion> {

    /**
     * 找/创建产品当前 DRAFT 草稿行,并按当前产品树重新生成 snapshot 写回。草稿生命周期单一入口:任意物模型
     * CRUD 提交后统一调本方法刷新草稿;每个产品同时只有一个 DRAFT 行。
     *
     * @param productIdentification 产品标识
     * @return 刷新后的 DRAFT 草稿行
     */
    ProductVersion upsertDraft(String productIdentification);

    /**
     * 找产品当前 DRAFT 草稿行(不存在返 empty)。
     *
     * @param productIdentification 产品标识
     * @return DRAFT 草稿行;不存在返 empty
     */
    Optional<ProductVersion> findDraft(String productIdentification);

    /**
     * 解析产品当前草稿版本号 —— 草稿不存在则创建。
     *
     * @param productIdentification 产品标识
     * @return 草稿版本号
     */
    String resolveDraftVersion(String productIdentification);

    /**
     * 按产品标识 + 版本号定位一条版本快照。
     *
     * @param productIdentification 产品标识
     * @param versionNo             版本序号
     * @return 版本快照;不存在返 empty
     */
    Optional<ProductVersion> findByProductIdentificationAndVersionNo(String productIdentification, String versionNo);

    /**
     * 按产品标识查询所有版本(created_time 倒序)。
     *
     * @param productIdentification 产品标识
     * @return 版本列表(created_time 倒序)
     */
    List<ProductVersion> listByProductIdentification(String productIdentification);

    /**
     * 软删指定产品下的所有版本行(DRAFT / 已发布 / 历史全部级联清理),用于产品被删时避免幽灵草稿 + 历史快照
     * 残留。走逻辑删除物理行保留供审计;TD 资源不在本方法清理,调用方需先确认无设备引用。返回软删行数。
     *
     * @param productIdentification 产品标识
     * @return 软删行数
     */
    int softDeleteAllByProductIdentification(String productIdentification);

    /**
     * 发布新版本:雪花生成版本号 → 序列化快照 → 插入 {@link ProductVersion} → 更新 product.active_version_no
     * 指针 → 同步建 TD super table → 写 {@link ProductPublishRecord}。
     *
     * @param vo 发布参数
     * @return 新发布的版本行
     */
    ProductVersion publish(ProductVersionPublishVO vo);

    /**
     * 回滚到历史版本:切 product.active_version_no 指针 → 写 {@link ProductPublishRecord} 回滚记录;TD 资源不动。
     *
     * @param vo 回滚参数
     * @return 回滚后激活的版本行
     */
    ProductVersion rollback(ProductVersionRollbackVO vo);

    /**
     * 历史清理:删除指定版本的 TD 资源,{@link ProductVersion} 行保留。
     *
     * @param vo 清理参数
     * @return 清理后的版本行
     */
    ProductVersion purgeHistory(ProductVersionPurgeVO vo);

    /**
     * 计算两个版本的字段级 diff。sourceVersion 可为 null(表示与空快照对比)。
     *
     * @param productIdentification 产品标识
     * @param sourceVersion         源版本号,可为 null(表示与空快照对比)
     * @param targetVersion         目标版本号
     * @return 版本字段级 diff 结果
     */
    ProductVersionDiffVO diff(String productIdentification, String sourceVersion, String targetVersion);

    /**
     * 物模型版本管理总览统计:总产品数 / 已发布 / 灰度中 / 未发布 / 近 7 天发布次数。
     *
     * @return 版本管理总览统计结果
     */
    ProductVersionStatisticsResultVO statistics();
}
