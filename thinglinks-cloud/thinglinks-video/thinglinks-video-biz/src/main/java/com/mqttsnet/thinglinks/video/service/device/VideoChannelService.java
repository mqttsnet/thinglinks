package com.mqttsnet.thinglinks.video.service.device;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.device.VideoChannel;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;

import java.util.List;
import java.util.Map;

/**
 * 业务接口 - 统一通道表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
public interface VideoChannelService extends SuperService<Long, VideoChannel> {

    /**
     * 根据设备标识查询通道列表
     *
     * @param deviceIdentification 设备标识
     * @return 通道结果VO列表
     */
    List<VideoChannelResultVO> listByDeviceIdentification(String deviceIdentification);

    /**
     * 根据通道标识查询通道
     *
     * @param channelIdentification 通道标识
     * @return 通道结果VO
     */
    VideoChannelResultVO getByChannelIdentification(String channelIdentification);

    /**
     * 从 Catalog 应答批量同步通道（upsert）
     *
     * @param deviceIdentification 设备标识
     * @param channels             解析后的通道列表
     * @return 同步的通道数量
     */
    int syncChannelsFromCatalog(String deviceIdentification, List<VideoChannel> channels);

    /**
     * 更新通道在线状态
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     * @param online                在线状态
     */
    void updateOnlineStatus(String deviceIdentification, String channelIdentification, boolean online);

    /**
     * 按通道标识逻辑删除
     *
     * @param deviceIdentification  设备标识
     * @param channelIdentification 通道标识
     */
    void removeByChannelIdentification(String deviceIdentification, String channelIdentification);

    /**
     * 统计设备下的通道数量
     *
     * @param deviceIdentification 设备标识
     * @return 通道数
     */
    int countByDeviceIdentification(String deviceIdentification);
}
