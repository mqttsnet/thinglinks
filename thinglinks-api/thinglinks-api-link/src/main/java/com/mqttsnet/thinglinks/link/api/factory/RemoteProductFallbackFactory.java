package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.RemoteProductService;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @program: thinglinks
 * @description: 产品管理服务降级处理
 * @packagename: com.mqttsnet.thinglinks.link.api.factory
 * @author: shisen
 * @date: 2022-07-26
 **/
@Component
public class RemoteProductFallbackFactory implements FallbackFactory<RemoteProductService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteProductFallbackFactory.class);

    @Override
    public RemoteProductService create(Throwable throwable) {
        log.error("产品管理服务调用失败:{}", throwable.getMessage());
        return new RemoteProductService() {
            @Override
            public R selectByProductIdentification(String productIdentification) {
                return R.fail("查询产品失败", throwable.getMessage());
            }

            @Override
            public R selectProductServicesById(Long id) {
                return R.fail("查询产品服务信息失败", throwable.getMessage());
            }

            @Override
            public R selectByIdProperties(Long id) {
                return R.fail("查询产品属性失败", throwable.getMessage());
            }

            @Override
            public R selectAllProduct(String status){
                return R.fail("获取所有产品失败", throwable.getMessage());
            }

            @Override
            public R<?> selectProductByIdentificationList(List<String> identificationList){
                return R.fail("获取产品失败", throwable.getMessage());
            }
        };
    }

}
