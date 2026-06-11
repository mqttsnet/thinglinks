package com.mqttsnet.thinglinks.video.service.gateway.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.entity.gateway.VideoGatewayMapping;
import com.mqttsnet.thinglinks.video.manager.gateway.VideoGatewayMappingManager;
import com.mqttsnet.thinglinks.video.service.gateway.VideoGatewayMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 业务实现类 - 网关协议映射表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoGatewayMappingServiceImpl extends SuperServiceImpl<VideoGatewayMappingManager, Long, VideoGatewayMapping> implements VideoGatewayMappingService {
}
