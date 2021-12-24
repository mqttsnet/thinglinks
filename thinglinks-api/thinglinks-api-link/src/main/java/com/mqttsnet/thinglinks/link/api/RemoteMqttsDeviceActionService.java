package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.domain.device.MqttsDeviceAction;
import com.mqttsnet.thinglinks.link.api.factory.RemoteMqttsDeviceActionFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 设备动作服务
 *
 * @author thinglinks
 */
@FeignClient(contextId = "remoteMqttsDeviceActionService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteMqttsDeviceActionFallbackFactory.class)
public interface RemoteMqttsDeviceActionService {

    /**
     * 新增设备动作
     *
     * @param mqttsDeviceAction
     * @return
     */
    @PostMapping("/device/action/add")
    public R add(@RequestBody MqttsDeviceAction mqttsDeviceAction);
}
