package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.RemoteProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

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
            public AjaxResult selectByIdProperties(Long id) {
                return AjaxResult.error("查询产品属性失败", throwable.getMessage());
            }
        };
    }

}
