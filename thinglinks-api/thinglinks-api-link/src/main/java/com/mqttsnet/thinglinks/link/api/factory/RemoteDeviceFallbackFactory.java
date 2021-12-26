package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 设备管理服务降级处理
 *
 * @author shisen
 */
@Component
public class RemoteDeviceFallbackFactory implements FallbackFactory<RemoteDeviceService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteDeviceFallbackFactory.class);

    @Override
    public RemoteDeviceService create(Throwable throwable) {
        log.error("设备管理服务调用失败:{}", throwable.getMessage());
        return new RemoteDeviceService() {
            @Override
            public R<Device> findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(String clientId, String userName, String password, String deviceStatus, String protocolType) {
                return R.fail("认证接口失败:" + throwable.getMessage());
            }

            @Override
            public R updateConnectStatusByClientId(Device device) {
                return R.fail("更新设备在线状态失败:" + throwable.getMessage());
            }
        };
    }
}
