package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.link.api.factory.RemoteDeviceActionFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 设备动作服务
 *
 * @author thinglinks
 */
@FeignClient(contextId = "remoteDeviceActionService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteDeviceActionFallbackFactory.class)
public interface RemoteDeviceActionService {

    /**
     * 新增设备动作
     *
     * @param mqttsDeviceAction
     * @return
     */
    @PostMapping("/action")
    public R add(@RequestBody DeviceAction mqttsDeviceAction);
}
