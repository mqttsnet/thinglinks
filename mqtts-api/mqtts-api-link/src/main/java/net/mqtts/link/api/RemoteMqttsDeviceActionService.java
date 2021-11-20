package net.mqtts.link.api;

import net.mqtts.common.core.constant.ServiceNameConstants;
import net.mqtts.common.core.domain.R;
import net.mqtts.link.api.domain.MqttsDeviceAction;
import net.mqtts.link.api.factory.RemoteMqttsDeviceActionFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 设备动作服务
 *
 * @author mqtts
 */
@FeignClient(contextId = "remoteMqttsDeviceActionService", value = ServiceNameConstants.MQTTS_LINK, fallbackFactory = RemoteMqttsDeviceActionFallbackFactory.class)
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
