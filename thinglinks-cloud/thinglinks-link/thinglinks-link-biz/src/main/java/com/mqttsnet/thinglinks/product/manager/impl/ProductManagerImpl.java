package com.mqttsnet.thinglinks.product.manager.impl;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.product.entity.Product;
import com.mqttsnet.thinglinks.product.manager.ProductManager;
import com.mqttsnet.thinglinks.product.mapper.ProductMapper;
import com.mqttsnet.thinglinks.product.vo.query.ProductPageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用业务实现类
 * 产品模型
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductManagerImpl extends SuperManagerImpl<ProductMapper, Product> implements ProductManager {

    private final ProductMapper productMapper;

    @Override
    public IPage<Product> getPage(PageParams<ProductPageQuery> params) {
        IPage<Product> page = params.buildPage(Product.class);

        ProductPageQuery paramsModel = params.getModel();

        LbQueryWrap<Product> wrap = Wraps.lbQ();
        wrap.eq(StrUtil.isNotBlank(paramsModel.getProductIdentification()), Product::getProductIdentification, paramsModel.getProductIdentification())
                .in(!CollUtil.isEmpty(paramsModel.getProductIdentificationList()), Product::getProductIdentification, paramsModel.getProductIdentificationList());

        return productMapper.selectPage(page, wrap);
    }

    @Override
    public Product findOneByProductId(Long productId) {
        return productMapper.selectById(productId);
    }

    @Override
    public List<Product> getProductList(ProductPageQuery query) {
        QueryWrap<Product> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getAppId()), Product::getAppId, query.getAppId());
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getProductName()), Product::getProductName, query.getProductName());
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getManufacturerId()), Product::getManufacturerId, query.getManufacturerId());
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getModel()), Product::getModel, query.getModel());
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(query.getProductIdentification()), Product::getProductIdentification, query.getProductIdentification());
        queryWrap.lambda().in(CollUtil.isNotEmpty(query.getProductIdentificationList()), Product::getProductIdentification, query.getProductIdentificationList());
        queryWrap.lambda().eq(query.getProductStatus() != null, Product::getProductStatus, query.getProductStatus());
        return productMapper.selectList(queryWrap);
    }

    @Override
    public Product findOneByProductIdentification(String productIdentification) {
        QueryWrap<Product> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(productIdentification), Product::getProductIdentification, productIdentification);
        return productMapper.selectOne(queryWrap);
    }

    @Override
    public List<Product> findListByProductIdentificationList(List<String> productIdentificationList) {
        QueryWrap<Product> queryWrap = new QueryWrap<>();
        queryWrap.lambda().in(CollUtil.isNotEmpty(productIdentificationList), Product::getProductIdentification, productIdentificationList);
        return productMapper.selectList(queryWrap);
    }

    @Override
    public Product findOneByManufacturerIdAndModelAndDeviceType(String manufacturerId, String model, String deviceType) {
        QueryWrap<Product> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(manufacturerId), Product::getManufacturerId, manufacturerId);
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(model), Product::getModel, model);
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(deviceType), Product::getDeviceType, deviceType);
        return productMapper.selectOne(queryWrap);
    }

    /**
     * 获取产品模型总量
     *
     * @return {@link Long} 产品模型数据总量
     */
    @Override
    public Long findProductTotal() {
        return productMapper.selectCount(null);
    }

    @Override
    public Long countPublishedProducts() {
        LbQueryWrap<Product> wrap = Wraps.lbQ();
        wrap.isNotNull(Product::getActiveVersionNo).ne(Product::getActiveVersionNo, "");
        return productMapper.selectCount(wrap);
    }

    @Override
    public Long countCanaryInProgressProducts() {
        LbQueryWrap<Product> wrap = Wraps.lbQ();
        wrap.isNotNull(Product::getPreviousFullVersionNo).ne(Product::getPreviousFullVersionNo, "");
        return productMapper.selectCount(wrap);
    }

}


