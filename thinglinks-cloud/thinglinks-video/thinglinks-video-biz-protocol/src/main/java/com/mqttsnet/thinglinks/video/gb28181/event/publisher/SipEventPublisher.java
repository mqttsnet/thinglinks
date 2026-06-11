package com.mqttsnet.thinglinks.video.gb28181.event.publisher;

import cn.hutool.core.date.DateUtil;
import com.mqttsnet.basic.utils.SpringUtils;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoOfflineEvent;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoOnlineEvent;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceKeepaliveEvent;
import com.mqttsnet.thinglinks.video.dto.gb28181.SipTransactionInfo;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import org.springframework.stereotype.Component;

/**
 * @description: SIP Event事件通知
 * @author: mqttsnet
 */
@Component
public class SipEventPublisher {


    /**
     * 设备信息下线事件
     *
     * @param deviceResultVO 设备信息
     */
    public void deviceInfoOfflineEventPublish(VideoDeviceResultVO deviceResultVO) {
        DeviceInfoOfflineEvent outEvent = new DeviceInfoOfflineEvent(deviceResultVO);
        SpringUtils.publishEvent(outEvent);
    }


    /**
     * 设备信息上线事件
     *
     * @param deviceResultVO     设备信息
     * @param sipTransactionInfo SIP事务信息
     */
    public void deviceInfoOnlineEventPublish(VideoDeviceResultVO deviceResultVO, SipTransactionInfo sipTransactionInfo) {
        DeviceInfoOnlineEvent outEvent = new DeviceInfoOnlineEvent(deviceResultVO, sipTransactionInfo);
        SpringUtils.publishEvent(outEvent);
    }

    /**
     * 设备心跳事件。
     * <p>
     * REGISTER（含重复注册）/ Keepalive MESSAGE 等任何"设备还活着"的信号都应调用。
     * 由统一监听器刷新 last_keepalive_time 与 online_status，
     * 避免多处分散写 DB/Redis 造成漂移。
     *
     * @param deviceIdentification 设备 SIP ID
     * @param origin               心跳来源标识："REGISTER" / "KEEPALIVE" 等
     */
    public void deviceKeepaliveEventPublish(String deviceIdentification, String origin) {
        DeviceKeepaliveEvent event = new DeviceKeepaliveEvent(deviceIdentification, DateUtil.now(), origin);
        SpringUtils.publishEvent(event);
    }

}
