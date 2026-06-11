package com.mqttsnet.thinglinks.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.thinglinks.device.vo.result.ProductOverviewResultVO;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.query.ProductPageQuery;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;

import java.util.List;

/**
 * 产品基础信息只读查询 Service。独立于 {@link ProductService},不依赖任何下游 Service 实现,
 * 专供子项目 / 跨域 Service 反向查询、cache helper / 报表 / 导入导出等只读访问。
 *
 * <p>切库走 Service AOP 边界(实现类标注 @DS(BASE_TENANT)),且类图天然为 DAG,
 * 从根本规避反向依赖父 Service 形成的构造期循环依赖。</p>
 *
 * @author mqttsnet
 * @since 2026-05-18
 */
public interface ProductQueryService {

    /**
     * 根据产品 ID 查询产品基础信息,未命中返回 null。
     *
     * @param productId 产品 ID
     * @return 产品基础信息;未命中返 null
     */
    ProductResultVO findOneByProductId(Long productId);

    /**
     * 根据产品标识查询产品基础信息,未命中返回 null。
     *
     * @param productIdentification 产品标识
     * @return 产品基础信息;未命中返 null
     */
    ProductResultVO findOneByProductIdentification(String productIdentification);

    /**
     * 按条件查询产品列表。
     *
     * @param query 产品查询条件
     * @return 产品列表
     */
    List<ProductResultVO> getProductResultVOList(ProductPageQuery query);

    /**
     * 查询产品总数。
     *
     * @return 产品总数
     */
    Long findProductTotal();

    /**
     * 统计已发布过的产品数(active_version_no 非空)。
     * 跨域调用方走 Service 层确保 @DS(BASE_TENANT) 切租户库。
     *
     * @return 已发布产品数
     */
    Long countPublishedProducts();

    /**
     * 统计当前处于灰度切换中的产品数(previous_full_version_no 非空 ── 切换前的全量版本号被记录)。
     * 跨域调用方走 Service 层确保 @DS(BASE_TENANT) 切租户库。
     *
     * @return 灰度切换中的产品数
     */
    Long countCanaryInProgressProducts();

    /**
     * 统计物模型服务总数(product_service 表),无数据返回 0。
     * 跨域调用方走 Service 层确保 @DS(BASE_TENANT) 切租户库。
     *
     * @return 物模型服务总数;无数据返 0
     */
    Long countThingModelServices();

    /**
     * 分页查询产品。
     *
     * @param params 分页查询参数
     * @return 产品分页结果
     */
    IPage<ProductResultVO> getPage(PageParams<ProductPageQuery> params);

    /**
     * 查询产品概况(类型/状态统计)。
     *
     * @return 产品概况
     */
    ProductOverviewResultVO getProductOverview();

    /**
     * 查询产品聚合(含 services / commands / properties / requests / responses)的完整 VO,默认仅含启用状态的服务。
     * 实现内部走各子项目 Manager(非 Service),确保不引入下游 Service 反向依赖。
     *
     * @param productIdentification 产品标识
     * @return 产品聚合完整 VO
     */
    ProductParamVO selectFullProductByProductIdentification(String productIdentification);

    /**
     * 查询产品聚合的完整 VO,可指定是否包含停用状态的服务。
     * 物模型版本快照须为完整物模型(停用服务也算其一部分),传 true;运行时设备解析等场景按需仅取启用服务,传 false。
     *
     * @param includeInactiveServices true=含停用服务(全部),false=仅启用服务
     */
    ProductParamVO selectFullProductByProductIdentification(String productIdentification,
                                                            boolean includeInactiveServices);
}
