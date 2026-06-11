package com.mqttsnet.thinglinks.product.manager;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.thinglinks.product.entity.Product;
import com.mqttsnet.thinglinks.product.vo.query.ProductPageQuery;

/**
 * <p>
 * 通用业务接口
 * 产品模型
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
public interface ProductManager extends SuperManager<Product> {

    /**
     * 分页查询产品信息
     *
     * @param params 分页参数
     * @return {@link IPage<Product>} 分页数据
     */
    IPage<Product> getPage(PageParams<ProductPageQuery> params);

    /**
     * 根据产品模型ID查询信息
     *
     * @param productId 产品模型 ID
     * @return 产品模型信息;查不到返 null
     */
    Product findOneByProductId(Long productId);

    /**
     * 根据条件查询产品模型信息
     *
     * @param query 查询条件
     * @return Product 产品模型信息
     */
    List<Product> getProductList(ProductPageQuery query);

    /**
     * 根据产品标识查询信息
     *
     * @param productIdentification 产品标识
     * @return {@link List<Product>} 产品模型信息
     */
    Product findOneByProductIdentification(String productIdentification);

    /**
     * 根据产品标识集合查询信息
     *
     * @param productIdentificationList 产品标识集合
     * @return {@link List<Product>} 产品模型信息
     */
    List<Product> findListByProductIdentificationList(List<String> productIdentificationList);

    /**
     * 查询产品模型是否存在
     *
     * @param manufacturerId 厂商ID
     * @param model          产品型号
     * @param deviceType     设备类型
     * @return {@link Product} 产品模型信息
     */
    Product findOneByManufacturerIdAndModelAndDeviceType(String manufacturerId, String model, String deviceType);

    /**
     * 获取产品模型总量
     *
     * @return {@link Long} 产品模型数据总量
     */
    Long findProductTotal();

    /**
     * 统计已发布过的产品数(product_version 字段非空非空字符串)。
     *
     * <p>语义:产品至少经历过一次发布(publish 后会把 publishedVersion 回写到 product.active_version_no)。</p>
     *
     * @return 已发布过的产品数
     */
    Long countPublishedProducts();

    /**
     * 统计当前处于灰度切换中的产品数(previous_full_version_no 非空)。
     *
     * <p>语义:灰度发布把当前 activeVersionNo 切到新版本,同时把切换前的全量版本号记入 previous_full_version_no;
     * 当 previous_full_version_no 非空就表示该产品当前生效版本是灰度版本。</p>
     *
     * @return 灰度切换中的产品数
     */
    Long countCanaryInProgressProducts();
}


