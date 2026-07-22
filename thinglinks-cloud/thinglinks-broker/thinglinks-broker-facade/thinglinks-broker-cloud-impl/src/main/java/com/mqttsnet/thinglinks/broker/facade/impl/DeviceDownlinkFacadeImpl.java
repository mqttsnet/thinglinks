package com.mqttsnet.thinglinks.broker.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.broker.DeviceDownlinkFacade;
import com.mqttsnet.thinglinks.broker.api.DeviceDownlinkApi;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 设备下行派发 Facade ── cloud 部署:代理到 {@link DeviceDownlinkApi}(Feign 调 broker)。
 *
 * @author mqttsnet
 */
@Service
public class DeviceDownlinkFacadeImpl implements DeviceDownlinkFacade {

    @Autowired
    @Lazy
    private DeviceDownlinkApi deviceDownlinkApi;

    @Override
    public R<?> dispatch(DownlinkCommand command) {
        return deviceDownlinkApi.dispatch(command);
    }
}
