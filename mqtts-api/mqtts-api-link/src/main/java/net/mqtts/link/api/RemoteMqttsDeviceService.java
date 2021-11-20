package net.mqtts.link.api;

import net.mqtts.common.core.constant.ServiceNameConstants;
import net.mqtts.common.core.domain.R;
import net.mqtts.link.api.domain.MqttsDevice;
import net.mqtts.link.api.factory.RemoteMqttsDeviceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 设备管理服务
 *
 * @author shisen
 */
@FeignClient(contextId = "remoteMqttsDeviceService", value = ServiceNameConstants.MQTTS_LINK, fallbackFactory = RemoteMqttsDeviceFallbackFactory.class)
public interface RemoteMqttsDeviceService {

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
    public R<MqttsDevice> findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(@RequestParam(value = "clientId", required = true) String clientId,
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
    public R updateConnectStatusByClientId(@RequestBody MqttsDevice mqttsDevice);

}
