package com.mqttsnet.thinglinks.video.service.platform;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatform;

/**
 * Description:
 * 级联平台业务接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface VideoPlatformService extends SuperService<Long, VideoPlatform> {

    VideoPlatform getByServerGbId(String serverGbId);
}
