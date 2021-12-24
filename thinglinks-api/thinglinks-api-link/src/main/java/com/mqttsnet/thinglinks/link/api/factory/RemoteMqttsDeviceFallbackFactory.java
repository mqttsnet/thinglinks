package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.RemoteMqttsDeviceService;
import com.mqttsnet.thinglinks.link.api.domain.device.MqttsDevice;
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
public class RemoteMqttsDeviceFallbackFactory implements FallbackFactory<RemoteMqttsDeviceService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteMqttsDeviceFallbackFactory.class);

    @Override
    public RemoteMqttsDeviceService create(Throwable throwable) {
        log.error("设备管理服务调用失败:{}", throwable.getMessage());
        return new RemoteMqttsDeviceService() {
            @Override
            public R<MqttsDevice> findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(String clientId, String userName, String password, String deviceStatus, String protocolType) {
                return R.fail("认证接口失败:" + throwable.getMessage());
            }

            @Override
            public R updateConnectStatusByClientId(MqttsDevice mqttsDevice) {
                return R.fail("更新设备在线状态失败:" + throwable.getMessage());
            }
        };
    }
}
