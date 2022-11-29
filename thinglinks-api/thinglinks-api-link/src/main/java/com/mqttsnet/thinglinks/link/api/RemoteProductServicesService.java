package com.mqttsnet.thinglinks.link.api;


import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.factory.RemoteProductServicesFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@FeignClient(contextId = "remoteProductServicesService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteProductServicesFallbackFactory.class)
public interface RemoteProductServicesService {



    @GetMapping("/productServices/selectAllByProductIdentificationAndStatus")
    R<?> selectAllByProductIdentificationAndStatus(@RequestParam("productIdentification") String productIdentification,@RequestParam("status") String status);

    @PostMapping ("/productServices/selectServicesByServiceIdList")
    R<?> selectServicesByServiceIdList(@RequestBody List<Long> serviceIdList);
}
