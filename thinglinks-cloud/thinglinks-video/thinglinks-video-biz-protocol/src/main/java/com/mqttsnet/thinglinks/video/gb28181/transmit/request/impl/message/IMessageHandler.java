package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message;

import com.mqttsnet.thinglinks.video.dto.device.VideoDeviceInfoResultDTO;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import org.dom4j.Element;

import javax.sip.RequestEvent;

/**
 * @author mqttsnet
 */
public interface IMessageHandler {
    /**
     * 处理来自设备的信息
     *
     * @param evt        事件信息
     * @param deviceInfo 设备信息
     */
    void handForDevice(RequestEvent evt, VideoDeviceInfoResultDTO deviceInfo, Element element);

    /**
     * 处理来自平台的信息
     *
     * @param evt            事件信息
     * @param parentPlatform 上级平台信息
     */
    void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element element);
}
