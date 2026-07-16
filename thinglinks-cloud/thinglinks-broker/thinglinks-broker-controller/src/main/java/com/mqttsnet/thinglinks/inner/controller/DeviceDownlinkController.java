package com.mqttsnet.thinglinks.inner.controller;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.broker.downlink.DeviceDownlinkDispatchService;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备下行派发相关内部接口(inner)── 业务侧经 {@code DeviceDownlinkFacade} Feign 进来,
 * 按协议类型分流到 broker 各下发通道。
 *
 * @author mqttsnet
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/inner/deviceDownlinkOpen")
@Tag(name = "inner-设备下行派发")
public class DeviceDownlinkController {

    private final DeviceDownlinkDispatchService deviceDownlinkDispatchService;

    /**
     * 设备下行派发 ── 按协议类型分流下发到设备(MQTT / WebSocket / …)。
     *
     * @param command 协议无关下行命令
     * @return {@link R} 投递结果
     */
    @Operation(summary = "设备下行派发", description = "按协议类型分流下发到设备(MQTT / WebSocket / …)")
    @PostMapping("/dispatch")
    public R<?> dispatch(@Parameter(description = "下行命令", required = true) @RequestBody DownlinkCommand command) {
        return deviceDownlinkDispatchService.dispatch(command);
    }
}
