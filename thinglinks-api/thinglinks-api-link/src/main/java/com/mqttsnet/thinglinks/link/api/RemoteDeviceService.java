package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.factory.RemoteDeviceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 设备管理服务
 *
 * @author shisen
 */
@FeignClient(contextId = "remoteDeviceService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteDeviceFallbackFactory.class)
public interface RemoteDeviceService {

    /**
     * 认证接口
     *
     * @param clientId
     * @param userName
     * @param password
     * @param deviceStatus
     * @param protocolType
     * @return
     */
    @GetMapping("/device/findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType")
    public R<Device> findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(@RequestParam(value = "clientId", required = true) String clientId,
                                                                                           @RequestParam(value = "userName", required = true) String userName,
                                                                                           @RequestParam(value = "password", required = true) String password,
                                                                                           @RequestParam(value = "deviceStatus", required = true) String deviceStatus,
                                                                                           @RequestParam(value = "protocolType", required = true) String protocolType);

    /**
     * 更新设备在线状态
     *
     * @param mqttsDevice
     * @return
     */
    @PutMapping("/device/updateConnectStatusByClientId")
    public R updateConnectStatusByClientId(@RequestBody Device mqttsDevice);

}
