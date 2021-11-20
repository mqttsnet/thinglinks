package net.mqtts.link.api.factory;

import net.mqtts.common.core.domain.R;
import net.mqtts.link.api.RemoteMqttsDeviceActionService;
import net.mqtts.link.api.domain.MqttsDeviceAction;
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
public class RemoteMqttsDeviceActionFallbackFactory implements FallbackFactory<RemoteMqttsDeviceActionService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteMqttsDeviceActionFallbackFactory.class);

    @Override
    public RemoteMqttsDeviceActionService create(Throwable throwable) {
        log.error("设备消息服务调用失败:{}", throwable.getMessage());
        return new RemoteMqttsDeviceActionService() {
            @Override
            public R add(MqttsDeviceAction mqttsDeviceAction) {
                return R.fail("新增设备动作失败:" + throwable.getMessage());
            }
        };
    }
}
