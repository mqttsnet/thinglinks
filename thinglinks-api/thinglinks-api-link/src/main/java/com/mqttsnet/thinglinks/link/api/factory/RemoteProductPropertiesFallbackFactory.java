package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.RemoteProductPropertiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @program: thinglinks
 * @description: 产品属性管理服务降级处理
 * @packagename: com.mqttsnet.thinglinks.link.api.factory
 * @author: kk
 * @date: 2022-11-19
 **/
@Component
public class RemoteProductPropertiesFallbackFactory implements FallbackFactory<RemoteProductPropertiesService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteProductPropertiesFallbackFactory.class);

    @Override
    public RemoteProductPropertiesService create(Throwable throwable) {
        log.error("产品服务属性管理服务调用失败:{}", throwable.getMessage());
        return new RemoteProductPropertiesService() {
            @Override
            public R<?> selectAllByServiceId(@RequestParam("serviceId") Long serviceId) {
                return R.fail("产品服务属性", throwable.getMessage());
            }
            @Override
            public R<?> selectPropertiesByPropertiesIdList(List<Long> propertiesIdList){
                return R.fail("根据属性id列表获取属性失败", throwable.getMessage());
            }

        };
    }

}
