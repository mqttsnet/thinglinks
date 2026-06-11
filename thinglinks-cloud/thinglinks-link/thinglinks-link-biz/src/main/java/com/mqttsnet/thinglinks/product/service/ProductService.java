package com.mqttsnet.thinglinks.product.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.device.vo.result.ProductOverviewResultVO;
import com.mqttsnet.thinglinks.product.entity.Product;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.query.ProductPageQuery;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.product.vo.save.ProductSaveVO;
import com.mqttsnet.thinglinks.product.vo.update.ProductUpdateVO;
import org.springframework.web.multipart.MultipartFile;


/**
 * 产品模型业务接口。
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
public interface ProductService extends SuperService<Long, Product> {

    /**
     * 分页查询产品信息。
     *
     * @param params 分页查询参数
     * @return 产品分页结果
     */
    IPage<ProductResultVO> getPage(PageParams<ProductPageQuery> params);


    /**
     * 获取产品模型总量。
     *
     * @return 产品模型总数
     */
    Long findProductTotal();

    /**
     * 保存产品模型。
     *
     * @param saveVO 产品保存参数
     * @return 保存后的产品参数
     */
    ProductSaveVO saveProduct(ProductSaveVO saveVO);

    /**
     * 修改产品模型。
     *
     * @param updateVO 产品修改参数
     * @return 修改后的产品参数
     */
    ProductUpdateVO updateProduct(ProductUpdateVO updateVO);

    /**
     * 删除产品模型。
     *
     * @param id 产品 ID
     * @return 是否删除成功
     */
    Boolean deleteProduct(Long id);

    /**
     * 查询产品管理完整信息(包含服务、属性、命令)。
     *
     * @param productIdentification 产品标识
     * @return {@link ProductParamVO} 产品管理完整参数VO
     * @throws com.mqttsnet.basic.exception.BizException 产品不存在时抛出
     */
    ProductParamVO selectFullProductByProductIdentification(String productIdentification);

    /**
     * 导入产品模型 JSON 数据。
     *
     * @param file  产品模型 JSON 文件
     * @param appId 应用 ID
     */
    void importProductJson(MultipartFile file, String appId);

    /**
     * 根据产品 ID 查询产品详情。
     *
     * @param productId 产品 ID
     * @return 产品详情
     */
    ProductResultVO findOneByProductId(Long productId);

    /**
     * 根据产品标识查询产品详情。
     *
     * @param productIdentification 产品标识
     * @return 产品详情
     */
    ProductResultVO findOneByProductIdentification(String productIdentification);

    /**
     * 根据产品标识集合查询产品详情。
     *
     * @param productIdentificationList 产品标识集合
     * @return 产品详情列表
     */
    List<ProductResultVO> findListByProductIdentificationList(List<String> productIdentificationList);

    /**
     * 快捷生成产品模型。
     *
     * @param paramVO 产品参数
     */
    void generateProductJson(ProductParamVO paramVO);

    /**
     * 获取产品概况统计。
     *
     * @return 产品概况统计
     */
    ProductOverviewResultVO getProductOverview();

    /**
     * 初始化产品基础 Topic。
     *
     * @param productIdentification 产品标识
     * @param reInit                是否重新初始化
     * @return 是否初始化成功
     */
    Boolean initProductBaseTopics(String productIdentification, Boolean reInit);

    /**
     * 获取产品模型列表。
     *
     * @param query 产品查询条件
     * @return 产品模型列表
     */
    List<ProductResultVO> getProductResultVOList(ProductPageQuery query);

    /**
     * 发布场景 ── 切换产品当前生效版本号(指针)。更新 active_version_no 为 newActiveVersion;
     * previous_full_version_no 按 recordCurrentAsPrevious 处理。
     *
     * <p>跨域调用方(productversion 域)必须走本 Service 而非直接调 ProductManager ──
     * 否则跨层级 + 无 @DS 切库会 fallback 默认库。</p>
     *
     * @param productIdentification   产品标识
     * @param newActiveVersion        新的生效版本号
     * @param recordCurrentAsPrevious true=灰度发布(把切换前的 activeVersionNo 写入 previousFullVersionNo,供回滚 / 灰度路由);
     *                                false=全量发布(previousFullVersionNo 不动)
     * @return 更新后的 {@link Product} 实体
     */
    Product switchActiveVersionForPublish(String productIdentification, String newActiveVersion,
                                          boolean recordCurrentAsPrevious);

    /**
     * 回滚场景 ── 切换产品当前生效版本号(指针)到目标版本,同时清空 previous_full_version_no:
     * 回滚后产品不再处于"灰度切换中",必须清空备忘指针,避免后续 statistics 仍把它统计为"灰度中"。
     *
     * @param productIdentification 产品标识
     * @param targetVersion         回滚到的目标版本号
     * @return 更新后的 {@link Product} 实体
     */
    Product rollbackActiveVersion(String productIdentification, String targetVersion);
}