package com.mqttsnet.thinglinks.video.manager.platform.impl;

import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatformCatalog;
import com.mqttsnet.thinglinks.video.manager.platform.VideoPlatformCatalogManager;
import com.mqttsnet.thinglinks.video.mapper.platform.VideoPlatformCatalogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 级联平台目录 Manager 实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoPlatformCatalogManagerImpl extends SuperManagerImpl<VideoPlatformCatalogMapper, VideoPlatformCatalog> implements VideoPlatformCatalogManager {

    @Override
    public List<VideoPlatformCatalog> listByPlatformId(Long platformId) {
        QueryWrap<VideoPlatformCatalog> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(VideoPlatformCatalog::getPlatformId, platformId);
        return list(queryWrap);
    }
}
