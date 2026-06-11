package com.mqttsnet.thinglinks.broker;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.broker.downlink.DeviceDownlinkDispatchService;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 设备下行派发 Facade ── boot 部署:本地直调 broker-biz 的 {@link DeviceDownlinkDispatchService}。
 *
 * @author mqttsnet
 */
@Service
@RequiredArgsConstructor
public class DeviceDownlinkFacadeImpl implements DeviceDownlinkFacade {

    private final DeviceDownlinkDispatchService deviceDownlinkDispatchService;

    @Override
    public R<?> dispatch(DownlinkCommand command) {
        return deviceDownlinkDispatchService.dispatch(command);
    }
}
