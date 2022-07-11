package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.factory.RemoteProtocolFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @program: thinglinks
 * @description: 协议管理服务
 * @packagename: com.mqttsnet.thinglinks.link.api
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-11 15:15
 **/
@FeignClient(contextId = "remoteProtocolService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteProtocolFallbackFactory.class)
public interface RemoteProtocolService {

    /**
     * 刷新协议脚本缓存
     * @return
     */
    @GetMapping("/protocol/protocolScriptCacheRefresh")
    public AjaxResult protocolScriptCacheRefresh();
}
