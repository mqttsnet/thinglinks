package com.mqttsnet.thinglinks.broker.api;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.broker.api.hystrix.DeviceDownlinkApiFallback;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 设备下行派发 ── 开放接口 Feign 客户端(cloud 部署)。对标 {@link MqttBrokerOpenInnerApi}。
 *
 * @author mqttsnet
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:thinglinks-broker-server}", fallback = DeviceDownlinkApiFallback.class, path = "/inner/deviceDownlinkOpen")
public interface DeviceDownlinkApi {

    /**
     * 设备下行派发。
     *
     * @param command 协议无关下行命令
     * @return {@link R} 投递结果
     */
    @Operation(summary = "设备下行派发", description = "按协议类型分流下发到设备(MQTT / WebSocket / …)")
    @PostMapping(path = "/dispatch", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<?> dispatch(@RequestBody DownlinkCommand command);
}
