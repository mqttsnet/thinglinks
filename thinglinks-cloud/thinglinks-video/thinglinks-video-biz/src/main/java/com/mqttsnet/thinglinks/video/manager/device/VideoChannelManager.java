package com.mqttsnet.thinglinks.video.manager.device;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.device.VideoChannel;

import java.util.List;

/**
 * 通用业务接口 - 统一通道表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
public interface VideoChannelManager extends SuperManager<VideoChannel> {

    /**
     * 根据设备标识查询通道列表
     *
     * @param deviceIdentification 设备标识
     * @return 通道列表
     */
    List<VideoChannel> listByDeviceIdentification(String deviceIdentification);

    /**
     * 根据通道标识查询通道
     *
     * @param channelIdentification 通道标识
     * @return 通道信息
     */
    VideoChannel getOneByChannelIdentification(String channelIdentification);

    /**
     * 根据设备标识和通道标识查询
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @return 通道信息
     */
    VideoChannel getOneByDeviceAndChannel(String deviceIdentification, String channelIdentification);
}
