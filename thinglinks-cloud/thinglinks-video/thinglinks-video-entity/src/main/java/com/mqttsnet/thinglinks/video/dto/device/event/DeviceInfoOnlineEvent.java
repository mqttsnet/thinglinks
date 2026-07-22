package com.mqttsnet.thinglinks.video.dto.device.event;

import com.mqttsnet.thinglinks.video.dto.gb28181.SipTransactionInfo;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import lombok.Getter;

/**
 * Description:
 * 设备信息上线事件对象
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2025/5/22
 */
@Getter
public class DeviceInfoOnlineEvent extends DeviceInfoBaseEventAbstract<VideoDeviceResultVO> {

    private final SipTransactionInfo sipTransactionInfo;

    public DeviceInfoOnlineEvent(VideoDeviceResultVO source, SipTransactionInfo sipTransactionInfo) {
        super(source);
        this.sipTransactionInfo = sipTransactionInfo;
    }
}
