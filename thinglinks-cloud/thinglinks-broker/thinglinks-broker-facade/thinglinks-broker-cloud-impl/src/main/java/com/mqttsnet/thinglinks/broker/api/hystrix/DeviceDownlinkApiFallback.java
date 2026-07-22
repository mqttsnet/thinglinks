package com.mqttsnet.thinglinks.broker.api.hystrix;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.broker.api.DeviceDownlinkApi;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;

import org.springframework.stereotype.Component;

/**
 * 设备下行派发 Feign 熔断降级。
 *
 * @author mqttsnet
 */
@Component
public class DeviceDownlinkApiFallback implements DeviceDownlinkApi {

    @Override
    public R<?> dispatch(DownlinkCommand command) {
        return R.timeout();
    }
}
