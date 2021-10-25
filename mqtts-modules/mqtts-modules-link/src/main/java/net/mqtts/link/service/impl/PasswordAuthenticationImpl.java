package net.mqtts.link.service.impl;


import io.github.quickmsg.common.auth.PasswordAuthentication;
import lombok.extern.slf4j.Slf4j;
import net.mqtts.link.common.enums.DeviceConnectStatus;
import net.mqtts.link.common.enums.DeviceStatus;
import net.mqtts.link.common.enums.ProtocolType;
import net.mqtts.link.domain.MqttsDevice;
import net.mqtts.link.service.IMqttsDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * @Description: MQTT 连接鉴权
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2021/10/23$ 23:09$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/10/23$ 23:09$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Service
@Slf4j
@Component
public class PasswordAuthenticationImpl implements PasswordAuthentication {

    private static PasswordAuthenticationImpl PasswordAuthenticationImpl;

    @Autowired
    private IMqttsDeviceService mqttsDeviceService;


    @PostConstruct
    public void init() {
        PasswordAuthenticationImpl = this;
        PasswordAuthenticationImpl.mqttsDeviceService = this.mqttsDeviceService;
    }

    /**
     * 认证接口
     *
     * @param userName         用户名称
     * @param passwordInBytes  密钥
     * @param clientIdentifier 设备标志
     * @return 布尔
     */
    @Override
    public boolean auth(String userName, byte[] passwordInBytes, String clientIdentifier) {
        MqttsDevice oneByClientIdAndUserNameAndPassword = PasswordAuthenticationImpl.mqttsDeviceService.findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(clientIdentifier, userName, new String(passwordInBytes), DeviceStatus.ENABLE.getValue(), ProtocolType.MQTT.getValue());
        if (Optional.ofNullable(oneByClientIdAndUserNameAndPassword).isPresent()){
            //更改设备在线状态为在线
            PasswordAuthenticationImpl.mqttsDeviceService.updateConnectStatusByClientId(DeviceConnectStatus.ONLINE.getValue(),clientIdentifier);
            return true;
        }
        return false;
    }


}
