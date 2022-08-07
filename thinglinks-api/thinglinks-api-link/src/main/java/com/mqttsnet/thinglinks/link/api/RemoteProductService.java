package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.factory.RemoteProductFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @program: thinglinks
 * @description: 产品管理服务
 * @packagename: com.mqttsnet.thinglinks.link.api
 * @author: shisen
 * @date: 2022-07-26
 **/
@FeignClient(contextId = "remoteProductService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteProductFallbackFactory.class)
public interface RemoteProductService {

    /**
     * 查询产品信息
     */
    /**
     * 查询属性信息
     *
     * @return
     */
    @GetMapping("/product/selectByProductIdentification")
    public R<?> selectByProductIdentification(String productIdentification);

    /**
     * 查询服务信息
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/productServices/{id}")
    public AjaxResult selectProductServicesById(@PathVariable("id") Long id);


    /**
     * 查询属性信息
     *
     * @return
     */
    @GetMapping("/productProperties/{id}")
    public AjaxResult selectByIdProperties(@PathVariable("id") Long id);
}
