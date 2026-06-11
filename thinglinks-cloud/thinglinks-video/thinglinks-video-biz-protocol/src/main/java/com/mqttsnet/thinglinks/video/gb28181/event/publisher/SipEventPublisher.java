package com.mqttsnet.thinglinks.video.gb28181.event.publisher;

import com.mqttsnet.basic.utils.SpringUtils;
import com.mqttsnet.thinglinks.video.dto.device.VideoDeviceInfoResultDTO;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoOfflineEvent;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoOnlineEvent;
import com.mqttsnet.thinglinks.video.dto.gb28181.SipTransactionInfo;
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
     * @param deviceInfoResultDTO 设备信息
     */
    public void deviceInfoOfflineEventPublish(VideoDeviceInfoResultDTO deviceInfoResultDTO) {
        DeviceInfoOfflineEvent outEvent = new DeviceInfoOfflineEvent(deviceInfoResultDTO);
        SpringUtils.publishEvent(outEvent);
    }


    /**
     * 设备信息上线事件
     *
     * @param deviceInfoResultDTO 设备信息
     * @param sipTransactionInfo  SIP事务信息
     */
    public void deviceInfoOnlineEventPublish(VideoDeviceInfoResultDTO deviceInfoResultDTO, SipTransactionInfo sipTransactionInfo) {
        DeviceInfoOnlineEvent outEvent = new DeviceInfoOnlineEvent(deviceInfoResultDTO, sipTransactionInfo);
        SpringUtils.publishEvent(outEvent);
    }

}
