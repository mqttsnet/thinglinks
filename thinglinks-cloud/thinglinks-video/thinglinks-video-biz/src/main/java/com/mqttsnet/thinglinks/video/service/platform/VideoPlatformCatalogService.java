package com.mqttsnet.thinglinks.video.service.platform;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatformCatalog;

import java.util.List;

/**
 * Description:
 * 级联平台目录业务接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface VideoPlatformCatalogService extends SuperService<Long, VideoPlatformCatalog> {

    List<VideoPlatformCatalog> listByPlatformId(Long platformId);
}
