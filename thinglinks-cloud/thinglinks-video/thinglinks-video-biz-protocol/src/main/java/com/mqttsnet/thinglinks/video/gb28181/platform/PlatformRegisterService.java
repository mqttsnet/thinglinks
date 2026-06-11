package com.mqttsnet.thinglinks.video.gb28181.platform;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.video.dto.gb28181.cache.PlatformRegisterCache;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatform;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.manager.platform.VideoPlatformManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import javax.sip.header.CallIdHeader;
import java.util.List;

/**
 * 平台级联注册服务（全局 Redis Hash 存储）。
 * <p>
 * 使用全局 Hash（非租户维度）存储注册状态，因为 @Scheduled 续期在无租户上下文时运行。
 * PlatformRegisterCache 中包含 tenantId，@Scheduled 遍历时恢复上下文。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class PlatformRegisterService {

    private final PlatformCascadeCommander cascadeCommander;
    private final SIPSender sipSender;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final VideoPlatformManager videoPlatformManager;

    /**
     * 手动启动级联注册（由 Controller 带租户上下文时调用）
     */
    public void startCascade(VideoPlatform platform) {
        if (platform == null || platform.getId() == null) {
            return;
        }
        putPlatformCache(platform, null);
        doRegister(platform);
        log.info("[平台级联] 手动启动级联注册: serverGbId={}", platform.getServerGbId());
    }

    /**
     * 停止级联注册
     */
    public void stopCascade(Long platformId) {
        PlatformRegisterCache cache = getPlatformCache(platformId);
        removePlatformCache(platformId);
        if (cache != null) {
            try {
                String localIp = StrUtil.isNotBlank(cache.getSipIp()) ? cache.getSipIp()
                        : tenantSipConfigProvider.resolve().getEffectiveHost();
                String transport = StrUtil.blankToDefault(cache.getTransport(), "UDP");
                CallIdHeader callId = sipSender.getNewCallIdHeader(localIp, transport);
                if (callId != null) {
                    VideoPlatform platform = cacheToPlatform(cache);
                    cascadeCommander.unregister(platform, callId);
                }
            } catch (Exception e) {
                log.warn("[平台级联] 注销失败: platformId={}", platformId, e);
            }
        }
    }

    /**
     * 定时续期注册（每 30 秒，从 Redis 遍历）
     */
    @Scheduled(fixedRate = 30000, initialDelay = 60000)
    public void refreshRegistrations() {
        List<PlatformRegisterCache> caches = getAllPlatformCaches();
        if (CollUtil.isEmpty(caches)) {
            return;
        }
        for (PlatformRegisterCache cache : caches) {
            try {
                if (cache.getTenantId() != null) {
                    ContextUtil.setTenantId(String.valueOf(cache.getTenantId()));
                }
                VideoPlatform platform = cacheToPlatform(cache);
                doRegister(platform);
            } catch (Exception e) {
                log.warn("[平台级联] 续期失败: serverGbId={}", cache.getServerGbId(), e);
            } finally {
                ContextUtil.setTenantId(null);
            }
        }
    }

    /**
     * 定时发送心跳 Keepalive（每 60 秒）
     */
    @Scheduled(fixedRate = 60000, initialDelay = 120000)
    public void sendKeepalives() {
        List<PlatformRegisterCache> caches = getAllPlatformCaches();
        if (CollUtil.isEmpty(caches)) {
            return;
        }
        for (PlatformRegisterCache cache : caches) {
            try {
                if (cache.getTenantId() != null) {
                    ContextUtil.setTenantId(String.valueOf(cache.getTenantId()));
                }
                VideoPlatform platform = cacheToPlatform(cache);
                cascadeCommander.keepalive(platform);
            } catch (Exception e) {
                log.warn("[平台级联] Keepalive 失败: serverGbId={}", cache.getServerGbId(), e);
            } finally {
                ContextUtil.setTenantId(null);
            }
        }
    }

    public void doRegister(VideoPlatform platform) {
        String localIp = resolveLocalIp(platform);
        String transport = StrUtil.blankToDefault(platform.getTransport(), "UDP");
        CallIdHeader callId = sipSender.getNewCallIdHeader(localIp, transport);
        // JAIN-SIP CallIdHeader 的 equals(null) 可能 NPE，Hutool ObjectUtil.isNull 会触发 equals，故此处用 == null
        if (callId == null) {
            log.warn("[平台级联] 获取 CallId 失败, 跳过注册: serverGbId={}", platform.getServerGbId());
            return;
        }
        String callIdStr = cascadeCommander.register(platform, callId);
        if (callIdStr != null) {
            putPlatformCache(platform, callIdStr);
        }
    }

    private String resolveLocalIp(VideoPlatform platform) {
        return StrUtil.isNotBlank(platform.getSipIp()) ? platform.getSipIp()
                : tenantSipConfigProvider.resolve().getEffectiveHost();
    }

    public String getCallId(Long platformId) {
        PlatformRegisterCache cache = getPlatformCache(platformId);
        return cache != null ? cache.getCallId() : null;
    }

    public void removeCallId(Long platformId) {
        PlatformRegisterCache cache = getPlatformCache(platformId);
        if (cache != null) {
            cache.setCallId(null);
            putCache(platformId, cache);
        }
    }

    public boolean isRegistered(Long platformId) {
        return getPlatformCache(platformId) != null;
    }

    // ==================== 通过 VideoPlatformManager 访问注册缓存 ====================

    private void putPlatformCache(VideoPlatform platform, String callId) {
        PlatformRegisterCache cache = PlatformRegisterCache.builder()
                .platformId(platform.getId())
                .tenantId(ContextUtil.getTenantId())
                .serverGbId(platform.getServerGbId())
                .serverGbDomain(platform.getServerGbDomain())
                .serverIp(platform.getServerIp())
                .serverPort(platform.getServerPort())
                .deviceGbId(platform.getDeviceGbId())
                .transport(platform.getTransport())
                .sipIp(platform.getSipIp())
                .sipPort(platform.getSipPort())
                .expires(platform.getExpires())
                .registerExpires(platform.getRegisterExpires())
                .username(platform.getUsername())
                .password(platform.getPassword())
                .callId(callId)
                .registeredAt(System.currentTimeMillis())
                .build();
        putCache(platform.getId(), cache);
    }

    private void putCache(Long platformId, PlatformRegisterCache cache) {
        videoPlatformManager.putRegisterCache(platformId, cache);
    }

    private PlatformRegisterCache getPlatformCache(Long platformId) {
        return videoPlatformManager.getRegisterCache(platformId).orElse(null);
    }

    private void removePlatformCache(Long platformId) {
        videoPlatformManager.removeRegisterCache(platformId);
    }

    private List<PlatformRegisterCache> getAllPlatformCaches() {
        return videoPlatformManager.listAllRegisterCaches();
    }

    private VideoPlatform cacheToPlatform(PlatformRegisterCache cache) {
        VideoPlatform platform = new VideoPlatform();
        platform.setId(cache.getPlatformId());
        platform.setServerGbId(cache.getServerGbId());
        platform.setServerGbDomain(cache.getServerGbDomain());
        platform.setServerIp(cache.getServerIp());
        platform.setServerPort(cache.getServerPort());
        platform.setDeviceGbId(cache.getDeviceGbId());
        platform.setTransport(cache.getTransport());
        platform.setSipIp(cache.getSipIp());
        platform.setSipPort(cache.getSipPort());
        platform.setExpires(cache.getExpires());
        platform.setRegisterExpires(cache.getRegisterExpires());
        platform.setUsername(cache.getUsername());
        platform.setPassword(cache.getPassword());
        return platform;
    }
}
