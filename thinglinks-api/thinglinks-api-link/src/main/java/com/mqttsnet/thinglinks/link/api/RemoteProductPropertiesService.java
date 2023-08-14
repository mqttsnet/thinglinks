package com.mqttsnet.thinglinks.link.api;


import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.factory.RemoteProductPropertiesFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(contextId = "RemoteProductPropertiesService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteProductPropertiesFallbackFactory.class)
public interface RemoteProductPropertiesService {

    @GetMapping("/productProperties/selectAllPropertiesByServiceId/{serviceId}")
    R<?> selectAllByServiceId(@RequestParam("serviceId") Long serviceId);

    @PostMapping("/productProperties/selectPropertiesByPropertiesIdList")
    R<?> selectPropertiesByPropertiesIdList(@RequestBody List<Long> propertiesIdList);
}
