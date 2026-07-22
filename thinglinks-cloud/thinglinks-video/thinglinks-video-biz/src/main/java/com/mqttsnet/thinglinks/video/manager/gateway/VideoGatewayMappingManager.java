package com.mqttsnet.thinglinks.video.manager.gateway;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.gateway.VideoGatewayMapping;

import java.util.List;

/**
 * 通用业务接口 - 网关协议映射表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
public interface VideoGatewayMappingManager extends SuperManager<VideoGatewayMapping> {

    /**
     * 根据源设备标识查询映射列表
     *
     * @param srcDeviceIdentification 源设备标识
     * @return 映射列表
     */
    List<VideoGatewayMapping> listBySrcDeviceIdentification(String srcDeviceIdentification);

    /**
     * 根据国标设备编号查询映射
     *
     * @param gbDeviceId 国标设备编号
     * @return 映射信息
     */
    VideoGatewayMapping getOneByGbDeviceId(String gbDeviceId);

    /**
     * 根据国标通道编号查询映射
     *
     * @param gbChannelId 国标通道编号
     * @return 映射信息
     */
    VideoGatewayMapping getOneByGbChannelId(String gbChannelId);
}
