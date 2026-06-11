package com.mqttsnet.thinglinks.video.manager.device;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.device.VideoDevice;

import java.util.List;

/**
 * 通用业务接口 - 统一设备表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
public interface VideoDeviceManager extends SuperManager<VideoDevice> {

    /**
     * 根据设备标识查询设备
     *
     * @param deviceIdentification 设备标识
     * @return 设备信息
     */
    VideoDevice getOneByDeviceIdentification(String deviceIdentification);

    /**
     * 根据接入协议查询设备列表
     *
     * @param accessProtocol 接入协议
     * @return 设备列表
     */
    List<VideoDevice> listByAccessProtocol(String accessProtocol);
}
