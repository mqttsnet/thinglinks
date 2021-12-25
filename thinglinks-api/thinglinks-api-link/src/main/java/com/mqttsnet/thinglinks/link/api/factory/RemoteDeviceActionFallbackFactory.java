package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceActionService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 设备动作服务降级处理
 *
 * @author shisen
 */
@Component
public class RemoteDeviceActionFallbackFactory implements FallbackFactory<RemoteDeviceActionService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteDeviceActionFallbackFactory.class);

    @Override
    public RemoteDeviceActionService create(Throwable throwable) {
        log.error("设备消息服务调用失败:{}", throwable.getMessage());
        return new RemoteDeviceActionService() {
            @Override
            public R add(DeviceAction mqttsDeviceAction) {
                return R.fail("新增设备动作失败:" + throwable.getMessage());
            }
        };
    }
}
