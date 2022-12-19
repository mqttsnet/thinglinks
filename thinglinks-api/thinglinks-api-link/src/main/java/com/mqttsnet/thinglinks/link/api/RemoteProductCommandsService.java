package com.mqttsnet.thinglinks.link.api;


import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.factory.RemoteProductCommandsFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;


@FeignClient(contextId = "remoteProductCommandsService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteProductCommandsFallbackFactory.class)
public interface RemoteProductCommandsService {


    @PostMapping ("/productCommands/selectProductCommandsByIdList")
    R<?> selectProductCommandsByIdList(@RequestBody List<Long> commandIdList);
}
