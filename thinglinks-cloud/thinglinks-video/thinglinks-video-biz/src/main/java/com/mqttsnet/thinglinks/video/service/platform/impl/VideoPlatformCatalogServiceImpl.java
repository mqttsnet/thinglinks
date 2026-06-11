package com.mqttsnet.thinglinks.video.service.platform.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatformCatalog;
import com.mqttsnet.thinglinks.video.manager.platform.VideoPlatformCatalogManager;
import com.mqttsnet.thinglinks.video.service.platform.VideoPlatformCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 级联平台目录业务实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoPlatformCatalogServiceImpl extends SuperServiceImpl<VideoPlatformCatalogManager, Long, VideoPlatformCatalog> implements VideoPlatformCatalogService {

    @Override
    public List<VideoPlatformCatalog> listByPlatformId(Long platformId) {
        return superManager.listByPlatformId(platformId);
    }
}
