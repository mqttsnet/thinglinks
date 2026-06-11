package com.mqttsnet.thinglinks.video.service.platform.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatform;
import com.mqttsnet.thinglinks.video.manager.platform.VideoPlatformManager;
import com.mqttsnet.thinglinks.video.service.platform.VideoPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Description:
 * 级联平台业务实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoPlatformServiceImpl extends SuperServiceImpl<VideoPlatformManager, Long, VideoPlatform> implements VideoPlatformService {

    @Override
    public VideoPlatform getByServerGbId(String serverGbId) {
        return superManager.getByServerGbId(serverGbId);
    }
}
