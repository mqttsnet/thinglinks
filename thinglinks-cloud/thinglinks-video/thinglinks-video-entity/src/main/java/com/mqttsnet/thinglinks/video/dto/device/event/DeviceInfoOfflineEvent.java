package com.mqttsnet.thinglinks.video.dto.device.event;

import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import lombok.Getter;

/**
 * Description:
 * 设备信息下线事件对象
 *
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2025/5/22
 */
@Getter
public class DeviceInfoOfflineEvent extends DeviceInfoBaseEventAbstract<VideoDeviceResultVO> {


    public DeviceInfoOfflineEvent(VideoDeviceResultVO source) {
        super(source);
    }
}
