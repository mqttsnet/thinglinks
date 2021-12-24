package com.mqttsnet.thinglinks.link.api.factory;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.RemoteMqttsDeviceDatasService;
import com.mqttsnet.thinglinks.link.api.domain.device.MqttsDeviceDatas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 设备消息服务降级处理
 *
 * @author shisen
 */
@Component
public class RemoteMqttsDeviceDatasFallbackFactory implements FallbackFactory<RemoteMqttsDeviceDatasService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteMqttsDeviceDatasFallbackFactory.class);

    @Override
    public RemoteMqttsDeviceDatasService create(Throwable throwable) {
        log.error("设备消息服务调用失败:{}", throwable.getMessage());
        return new RemoteMqttsDeviceDatasService() {
            @Override
            public R add(MqttsDeviceDatas mqttsDeviceDatas) {
                return R.fail("新增设备消息失败:" + throwable.getMessage());
            }

        };
    }
}
