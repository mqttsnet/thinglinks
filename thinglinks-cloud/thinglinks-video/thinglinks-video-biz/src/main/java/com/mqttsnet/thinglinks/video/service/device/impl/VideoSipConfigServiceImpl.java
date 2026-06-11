package com.mqttsnet.thinglinks.video.service.device.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigCacheVO;
import com.mqttsnet.thinglinks.video.entity.device.VideoSipConfig;
import com.mqttsnet.thinglinks.video.manager.device.VideoSipConfigManager;
import com.mqttsnet.thinglinks.video.service.device.VideoSipConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 租户 SIP 配置业务实现。
 * <p>
 * 核心职责: CRUD + Redis 缓存刷新 + sipId 唯一校验。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-10
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoSipConfigServiceImpl extends SuperServiceImpl<VideoSipConfigManager, Long, VideoSipConfig> implements VideoSipConfigService {

    @Override
    public List<VideoSipConfig> listEnabled() {
        return superManager.listEnabled();
    }

    @Override
    public void refreshTenantCache(Long tenantId) {
        List<VideoSipConfig> configs = superManager.listEnabled();
        for (VideoSipConfig config : configs) {
            TenantSipConfigCacheVO vo = buildCacheVO(config, tenantId);
            // 全局 Hash: sipId → config (只用一个 Hash，入站和出站都查它)
            superManager.putSipConfigCache(config.getSipId(), vo);
        }
        log.info("[SIP 缓存] 租户 {} 刷新完成, 配置数={}", tenantId, configs.size());
    }

    @Override
    public void refreshAllTenantCache() {
        // 清空全局 Hash
        superManager.clearAllSipConfigCache();
        // 当前租户刷新（由 Job 遍历所有租户调用）
        Long tenantId = ContextUtil.getTenantId();
        if (ObjectUtil.isNotNull(tenantId)) {
            refreshTenantCache(tenantId);
        }
    }

    @Override
    public void setDefault(Long id) {
        VideoSipConfig config = superManager.getById(id);
        if (config == null) {
            return;
        }
        // 取消旧默认
        QueryWrap<VideoSipConfig> wrap = new QueryWrap<>();
        wrap.lambda().eq(VideoSipConfig::getIsDefault, 1);
        List<VideoSipConfig> oldDefaults = superManager.list(wrap);
        for (VideoSipConfig old : oldDefaults) {
            old.setIsDefault(0);
            superManager.updateById(old);
        }
        // 设置新默认
        config.setIsDefault(1);
        superManager.updateById(config);
        // 刷 Redis
        refreshTenantCache(ContextUtil.getTenantId());
    }

    @Override
    public boolean isSipIdExists(String sipId) {
        return superManager.existsSipConfigCache(sipId);
    }

    @Override
    public String getDefaultSipId() {
        QueryWrap<VideoSipConfig> wrap = new QueryWrap<>();
        wrap.lambda()
                .eq(VideoSipConfig::getIsDefault, 1)
                .eq(VideoSipConfig::getStatus, 1)
                .last("LIMIT 1");
        VideoSipConfig config = superManager.getOne(wrap);
        return config != null ? config.getSipId() : null;
    }

    private TenantSipConfigCacheVO buildCacheVO(VideoSipConfig config, Long tenantId) {
        return TenantSipConfigCacheVO.builder()
                .tenantId(tenantId)
                .sipId(config.getSipId())
                .sipDomain(config.getSipDomain())
                .sipPassword(config.getSipPassword())
                .sipServerAddress(config.getSipServerAddress())
                .bindIp(config.getBindIp())
                .isDefault(config.getIsDefault())
                .registerInterval(config.getRegisterInterval())
                .configName(config.getConfigName())
                .build();
    }
}
