package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.factory.RemoteDeviceInfoFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 子设备管理服务
 *
 * @author ShiHuan SUN
 */
@FeignClient(contextId = "remoteDeviceInfoService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteDeviceInfoFallbackFactory.class)
public interface RemoteDeviceInfoService {

    /**
     * 刷新子设备数据模型
     * @param ids
     * @return
     */
    @GetMapping("/deviceInfo/refreshDeviceInfoDataModel")
    public AjaxResult refreshDeviceInfoDataModel(@RequestParam(name = "ids",required = false) Long[] ids);

}
