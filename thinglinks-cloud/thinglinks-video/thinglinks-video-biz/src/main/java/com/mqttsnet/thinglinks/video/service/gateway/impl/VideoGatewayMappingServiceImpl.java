package com.mqttsnet.thinglinks.video.service.gateway.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.entity.gateway.VideoGatewayMapping;
import com.mqttsnet.thinglinks.video.manager.gateway.VideoGatewayMappingManager;
import com.mqttsnet.thinglinks.video.service.gateway.VideoGatewayMappingService;
import com.mqttsnet.thinglinks.video.service.support.VideoIdentityNormalizer;
import com.mqttsnet.thinglinks.video.vo.update.gateway.VideoGatewayMappingUpdateVO;
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

    @Override
    protected <SaveVO> VideoGatewayMapping saveBefore(SaveVO saveVO) {
        return normalizeIdentity(super.saveBefore(saveVO));
    }

    @Override
    protected <UpdateVO> VideoGatewayMapping updateBefore(UpdateVO updateVO) {
        return normalizeIdentity(super.updateBefore(updateVO));
    }

    @Override
    protected <UpdateVO> void updateAfter(UpdateVO updateVO, VideoGatewayMapping mapping) {
        if (updateVO instanceof VideoGatewayMappingUpdateVO gatewayUpdateVO
                && VideoIdentityNormalizer.isAsciiSpaceOnly(gatewayUpdateVO.getSrcChannelIdentification())) {
            superManager.update(Wrappers.<VideoGatewayMapping>update()
                    .set("src_channel_identification", null)
                    .eq("id", mapping.getId())
                    .eq("deleted", 0));
        }
    }

    private VideoGatewayMapping normalizeIdentity(VideoGatewayMapping mapping) {
        mapping.setSrcProtocol(StrUtil.trim(mapping.getSrcProtocol()));
        mapping.setSrcDeviceIdentification(StrUtil.trim(mapping.getSrcDeviceIdentification()));
        mapping.setSrcChannelIdentification(
                VideoIdentityNormalizer.trimAsciiSpaceToNull(mapping.getSrcChannelIdentification()));
        return mapping;
    }
}
