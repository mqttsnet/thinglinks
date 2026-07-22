package com.mqttsnet.thinglinks.video.gb28181.session;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.cache.lock.DistributedLock;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.lock.video.VideoLockKeyBuilder;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.config.UserSetting;
import com.mqttsnet.thinglinks.video.enumeration.stream.SsrcPrefixEnum;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.SsrcEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SsrcAllocatedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SsrcPoolExhaustedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SsrcReleasedEventSource;
import com.mqttsnet.thinglinks.video.manager.ssrc.SsrcPoolManager;
import com.mqttsnet.thinglinks.video.manager.ssrc.SsrcTransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Description:
 * SSRC 池业务服务。
 * 管理 SSRC 的生成、分配和释放，基于 GB28181 标准：
 * - SSRC 为 10 位十进制数
 * - 第 1 位：0=实时流，1=历史流
 * - 第 2-5 位：SIP 域（domain 后 4 位）
 * - 第 6-10 位：序号（00001~99999）
 * <p>
 * 每个流媒体服务器有独立的 SSRC 池，支持多实例共享（Redis 存储）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class SsrcPoolService {

    private final SsrcPoolManager ssrcPoolManager;
    private final SsrcTransactionManager ssrcTransactionManager;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final UserSetting userSetting;
    private final SsrcEventPublisher ssrcEventPublisher;
    private final DistributedLock distributedLock;

    /**
     * SSRC 序号最大值
     */
    private static final int MAX_SSRC_SEQ = 9999;

    /**
     * 初始化指定流媒体服务器的 SSRC 池。
     * 生成实时流和历史流两组 SSRC 并写入 Redis。
     *
     * @param mediaIdentification 流媒体服务器标识
     */
    public void initPool(String mediaIdentification) {
        String domainSuffix = getDomainSuffix();
        Set<String> ssrcSet = new LinkedHashSet<>();

        // 生成实时流 SSRC：0 + 域后4位 + 5位序号
        for (int i = 1; i <= MAX_SSRC_SEQ; i++) {
            ssrcSet.add(SsrcPrefixEnum.PLAY.getPrefix() + domainSuffix + String.format("%05d", i));
        }

        // 生成历史流 SSRC：1 + 域后4位 + 5位序号
        for (int i = 1; i <= MAX_SSRC_SEQ; i++) {
            ssrcSet.add(SsrcPrefixEnum.PLAYBACK.getPrefix() + domainSuffix + String.format("%05d", i));
        }

        ssrcPoolManager.initPool(mediaIdentification, ssrcSet);
        log.info("SSRC池初始化完成: mediaIdentification={}, 实时流={}, 历史流={}, 总数={}",
                mediaIdentification, MAX_SSRC_SEQ, MAX_SSRC_SEQ, ssrcSet.size());
    }

    /**
     * 分配一个 SSRC
     *
     * @param mediaIdentification 流媒体服务器标识
     * @param prefix        SSRC 前缀（实时/历史）
     * @param deviceIdentification      设备编号
     * @param channelIdentification     通道编号
     * @return 分配到的 SSRC
     * @throws BizException 池耗尽时抛出异常
     */
    public String allocateSsrc(String mediaIdentification, SsrcPrefixEnum prefix, String deviceIdentification, String channelIdentification) {
        String usedBy = deviceIdentification + ":" + channelIdentification;
        // 直接按前缀一次性找匹配的 FREE，不做 allocate-release 蠢循环。
        // 之前先 allocate(任意) → 不匹配则 release → allocateWithPrefix 循环 9999 次，
        // 每次都 hGetAll 全量 map + HashMap 迭代顺序稳定 → 死循环返回同一个不匹配项。
        String ssrc = ssrcPoolManager.allocate(mediaIdentification, prefix.getPrefix(), usedBy);

        if (StrUtil.isBlank(ssrc)) {
            // 发布池耗尽事件
            ssrcEventPublisher.publishSsrcPoolExhaustedEvent(SsrcPoolExhaustedEventSource.builder()
                    .mediaIdentification(mediaIdentification)
                    .poolSize(ssrcPoolManager.getPoolSize(mediaIdentification))
                    .build());

            throw BizException.wrap("SSRC池中前缀为 " + prefix.getPrefix() + " (" + prefix.getDesc()
                    + ") 的SSRC已耗尽: mediaIdentification=" + mediaIdentification);
        }

        // 发布分配成功事件
        ssrcEventPublisher.publishSsrcAllocatedEvent(SsrcAllocatedEventSource.builder()
                .mediaIdentification(mediaIdentification)
                .deviceIdentification(deviceIdentification)
                .channelIdentification(channelIdentification)
                .ssrc(ssrc)
                .build());

        log.info("SSRC分配成功: mediaIdentification={}, ssrc={}, deviceIdentification={}, channelIdentification={}",
                mediaIdentification, ssrc, deviceIdentification, channelIdentification);
        return ssrc;
    }

    /**
     * 释放 SSRC
     *
     * @param mediaIdentification 流媒体服务器标识
     * @param ssrc          要释放的 SSRC
     */
    public void releaseSsrc(String mediaIdentification, String ssrc) {
        if (StrUtil.isBlank(ssrc)) {
            return;
        }
        ssrcPoolManager.release(mediaIdentification, ssrc);

        // 发布释放事件
        ssrcEventPublisher.publishSsrcReleasedEvent(SsrcReleasedEventSource.builder()
                .mediaIdentification(mediaIdentification)
                .ssrc(ssrc)
                .build());

        log.info("SSRC释放: mediaIdentification={}, ssrc={}", mediaIdentification, ssrc);
    }

    /**
     * 获取可用 SSRC 数量
     *
     * @param mediaIdentification 流媒体服务器标识
     * @return 可用数量
     */
    public int getAvailableCount(String mediaIdentification) {
        return ssrcPoolManager.getAvailableCount(mediaIdentification);
    }

    /**
     * 获取指定流媒体服务器当前所有已分配 SSRC 的 {ssrc → usedBy} 映射。
     * usedBy 通常为 "deviceIdentification:channelIdentification"。
     * <p>供 ZLM Hook on_stream_none_reader 反查会话归属用，Controller 不直接调 Manager。
     */
    public Map<String, String> getAllocationsByMediaIdentification(String mediaIdentification) {
        return ssrcPoolManager.getAllAllocations(mediaIdentification);
    }

    /**
     * 判断指定设备是否还有活跃的 SSRC 事务（推流/播放中）。
     * <p>供删除设备前的活跃会话校验用，Controller 通过本方法访问，避免直接持 Manager。
     */
    public boolean hasActiveSessions(String deviceIdentification) {
        if (StrUtil.isBlank(deviceIdentification)) {
            return false;
        }
        var transactions = ssrcTransactionManager.getAll(deviceIdentification);
        return transactions != null && !transactions.isEmpty();
    }

    /**
     * 确保 SSRC 池已初始化：池不存在或容量为 0 时自动 {@link #initPool} 补齐。
     * <p>
     * 业务调用 {@link #allocateSsrc} 前先调一次本方法，避免 Redis 被清 / 服务首次启动后
     * 池未建导致 "SSRC池已耗尽" 假报警。失败会抛 BizException，让前端看到真实原因
     * （通常是 SIP 域配置缺失、Redis 不可达、租户上下文丢失等）。
     */
    public void ensurePoolInitialized(String mediaIdentification) {
        int poolSize = ssrcPoolManager.getPoolSize(mediaIdentification);
        if (poolSize == 0) {
            log.warn("[SSRC池] 首次发现池未初始化，自动补建: mediaIdentification={}", mediaIdentification);
            try {
                initPool(mediaIdentification);
                int newSize = ssrcPoolManager.getPoolSize(mediaIdentification);
                log.info("[SSRC池] 自动补建完成: mediaIdentification={}, poolSize={}", mediaIdentification, newSize);
                if (newSize == 0) {
                    // initPool 没抛异常但 Redis 里仍然为空 —— 通常是 key builder 跨上下文问题
                    throw BizException.wrap("SSRC池自动补建后仍为空，疑似租户上下文缺失或 Redis 写入失败: "
                            + mediaIdentification);
                }
            } catch (Exception e) {
                log.error("[SSRC池] 自动补建失败: mediaIdentification={}, error={}",
                        mediaIdentification, e.getMessage(), e);
                throw e instanceof BizException ? (BizException) e
                        : BizException.wrap("SSRC池自动补建失败: " + e.getMessage());
            }
        }
    }

    /**
     * 紧急重置：清空指定流媒体服务器的 SSRC 池 + 相关事务，随后重新初始化池容量。
     * <p>
     * 这是 nuclear option，仅管理员紧急恢复使用。会导致所有进行中的播放会话残留
     * 在设备侧（BYE 未下发），但 SSRC 池立即恢复可用。
     * <p>
     * 集群安全：内部通过 {@link DistributedLock} 按 mediaIdentification 粒度互斥。
     *
     * @param mediaIdentification 流媒体服务器标识
     */
    public void resetAndReinit(String mediaIdentification) {
        CacheKey lockKey = VideoLockKeyBuilder.forSsrcAdmin(mediaIdentification);
        distributedLock.tryLockAndRun(
                lockKey.getKey(),
                lockKey.getExpire().getSeconds(),
                TimeUnit.SECONDS,
                () -> {
                    // 1. 收集所有关联设备（从 pool 当前分配中提取）
                    Map<String, String> allocations = ssrcPoolManager.getAllAllocations(mediaIdentification);
                    Set<String> deviceIds = allocations.values().stream()
                            .map(SsrcPoolService::parseDeviceId)
                            .filter(d -> d != null && !d.isBlank())
                            .collect(Collectors.toSet());

                    // 2. 清空池
                    ssrcPoolManager.resetPool(mediaIdentification);

                    // 3. 清空这些设备的所有 SsrcTransaction
                    for (String deviceId : deviceIds) {
                        try {
                            ssrcTransactionManager.removeAll(deviceId);
                        } catch (Exception e) {
                            log.warn("[SSRC重置] 清理设备事务失败: deviceIdentification={}, error={}",
                                    deviceId, e.getMessage());
                        }
                    }

                    // 4. 重新初始化池（需要 SIP domain 配置，只能在协议层执行）
                    initPool(mediaIdentification);
                    log.warn("[SSRC重置] 完成: mediaIdentification={}, 清理设备数={}, 原分配数={}",
                            mediaIdentification, deviceIds.size(), allocations.size());
                    return null;
                });
    }

    /**
     * 从 usedBy 字符串（格式 "deviceIdentification:channelIdentification"）中解析 deviceIdentification。
     */
    private static String parseDeviceId(String usedBy) {
        if (usedBy == null || usedBy.isBlank()) {
            return null;
        }
        int idx = usedBy.indexOf(':');
        return idx <= 0 ? usedBy : usedBy.substring(0, idx);
    }

    /**
     * 获取域后缀（domain 后 4 位）
     */
    private String getDomainSuffix() {
        TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();
        String domain = tenantConfig.getDomain();
        if (StrUtil.isBlank(domain) || domain.length() < 4) {
            log.warn("SIP domain配置不规范，使用默认值: domain={}", domain);
            return "0000";
        }
        return domain.substring(domain.length() - 4);
    }

    /**
     * @deprecated 已废弃：低效 allocate-release 循环，改用 {@code SsrcPoolManager.allocate(mediaId, prefix, usedBy)}。
     */
    @Deprecated
    private String allocateWithPrefix(String mediaIdentification, SsrcPrefixEnum prefix, String usedBy) {
        // 多次尝试分配
        for (int i = 0; i < MAX_SSRC_SEQ; i++) {
            String ssrc = ssrcPoolManager.allocate(mediaIdentification, usedBy);
            if (StrUtil.isBlank(ssrc)) {
                return null;
            }
            if (ssrc.startsWith(prefix.getPrefix())) {
                return ssrc;
            }
            // 不匹配，释放回去
            ssrcPoolManager.release(mediaIdentification, ssrc);
        }
        return null;
    }
}
