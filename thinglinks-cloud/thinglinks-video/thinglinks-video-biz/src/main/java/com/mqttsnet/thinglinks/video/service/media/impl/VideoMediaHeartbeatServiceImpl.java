package com.mqttsnet.thinglinks.video.service.media.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.media.abl.ABLMediaServerStatusManager;
import com.mqttsnet.thinglinks.video.media.zlm.ZLMMediaServerStatusManager;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaHeartbeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 流媒体心跳 Service 实现，对外只是薄薄一层 delegate——
 * 真实业务在各协议的 StatusManager 里。
 *
 * <p>{@code @DS} 在每个公共方法上补一遍：StatusManager 内部已有 @DS（类级），但 boot-impl 通过本 Service 调进来时
 * AOP 入口在本类，本类的 @DS 才是切库主入口。
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoMediaHeartbeatServiceImpl implements VideoMediaHeartbeatService {

    private final ZLMMediaServerStatusManager zlmMediaServerStatusManager;
    private final ABLMediaServerStatusManager ablMediaServerStatusManager;

    @DS(DsConstant.BASE_TENANT)
    @Override
    public void zlmHeartbeat(Long tenantId) {
        zlmMediaServerStatusManager.executeForTenant(tenantId);
    }

    @DS(DsConstant.BASE_TENANT)
    @Override
    public void ablHeartbeat(Long tenantId) {
        ablMediaServerStatusManager.executeForTenant(tenantId);
    }

    @DS(DsConstant.BASE_TENANT)
    @Override
    public void refreshAll(Long tenantId) {
        zlmMediaServerStatusManager.executeForTenant(tenantId);
        ablMediaServerStatusManager.executeForTenant(tenantId);
    }
}
