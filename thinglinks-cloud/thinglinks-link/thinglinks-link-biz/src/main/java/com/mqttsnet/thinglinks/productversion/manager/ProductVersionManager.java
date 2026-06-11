package com.mqttsnet.thinglinks.productversion.manager;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;
import com.mqttsnet.thinglinks.productversion.vo.query.ProductVersionPageQuery;

/**
 * 产品物模型版本快照通用业务接口。
 *
 * @author mqttsnet
 * @see ProductVersion
 */
public interface ProductVersionManager extends SuperManager<ProductVersion> {

    /**
     * 分页查询版本列表。
     *
     * @param params 分页参数
     * @return {@link IPage} 分页结果
     */
    IPage<ProductVersion> getPage(PageParams<ProductVersionPageQuery> params);

    /**
     * 按产品标识 + 版本号定位一条版本快照。
     *
     * @param productIdentification 产品标识
     * @param versionNo               版本序号
     * @return {@link Optional} 版本快照
     */
    Optional<ProductVersion> findByProductIdentificationAndVersionNo(String productIdentification, String versionNo);

    /**
     * 按产品标识查询所有版本(按 created_time 倒序)。
     *
     * @param productIdentification 产品标识
     * @return {@link List} 版本快照列表
     */
    List<ProductVersion> listByProductIdentification(String productIdentification);

    /**
     * 按产品标识 + 状态查询版本。
     *
     * @param productIdentification 产品标识
     * @param versionStatus         版本状态值
     * @return {@link List} 版本快照列表
     */
    List<ProductVersion> listByProductIdentificationAndStatus(String productIdentification, Integer versionStatus);

    /**
     * 查找产品当前 DRAFT 草稿行(每个产品有且仅有一个)。
     *
     * <p>产品树 CRUD(product / service / property / command) 提交时,统一调
     * ProductVersionService.upsertDraft(...) 刷新这一行的 snapshot;
     * publish 时把当前 DRAFT 升级为目标状态(PUBLISHED/CANARY/SHADOW)然后再起一个新 DRAFT。</p>
     *
     * @param productIdentification 产品标识
     * @return {@link Optional} DRAFT 行(不存在返 empty)
     */
    Optional<ProductVersion> findDraft(String productIdentification);

    /**
     * 按版本状态统计版本总量(跨产品)。
     *
     * @param versionStatus 版本状态值(见 ProductVersionStatusEnum#getValue())
     * @return 该状态的版本数;入参空或无数据返回 0
     */
    Long countByVersionStatus(Integer versionStatus);
}
