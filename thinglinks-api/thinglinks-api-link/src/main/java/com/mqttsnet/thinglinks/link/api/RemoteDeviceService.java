package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.factory.RemoteDeviceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备管理服务
 *
 * @author shisen
 */
@FeignClient(contextId = "remoteDeviceService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteDeviceFallbackFactory.class)
public interface RemoteDeviceService {

    /**
     * 更新设备在线状态
     *
     * @param device
     * @return
     */
    @PutMapping("/device/updateConnectStatusByClientId")
    public R updateConnectStatusByClientId(@RequestBody Device device);


    /**
     * 客户端身份认证
     * @param params
     * @return
     */
    @PostMapping("/device/clientAuthentication")
    public R<Boolean> clientAuthentication(@RequestBody Map<String, Object> params);

}
