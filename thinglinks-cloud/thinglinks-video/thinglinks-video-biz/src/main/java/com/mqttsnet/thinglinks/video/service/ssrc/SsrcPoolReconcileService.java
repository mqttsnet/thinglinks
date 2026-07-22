package com.mqttsnet.thinglinks.video.service.ssrc;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.basic.cache.lock.DistributedLock;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.lock.video.VideoLockKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.SsrcTransaction;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.manager.ssrc.SsrcPoolManager;
import com.mqttsnet.thinglinks.video.manager.ssrc.SsrcTransactionManager;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.vo.query.media.VideoMediaServerPageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * SSRC 池孤儿对账服务（biz 层）。
 * <p>
 * 解决场景：页面关闭 / 网络中断 / 进程崩溃 等未走正常 stop 流程的情况下，
 * SSRC 被占用但对应 SsrcTransaction 已不存在或已销毁，导致 SSRC 池耗尽。
 * <p>
 * 核心算法：
 * <ol>
 *   <li>扫描流媒体服务器的 SSRC 池，取出所有非 free 的 {ssrc → usedBy} 映射。</li>
 *   <li>按 usedBy 中的 deviceIdentification 聚合，批量加载每个设备的 SsrcTransaction 列表。</li>
 *   <li>对每个已分配 SSRC，判断是否存在匹配 ssrc 的活跃事务；不存在则视为孤儿。</li>
 *   <li>孤儿 SSRC 直接通过 {@link SsrcPoolManager#release} 释放，回归池中。</li>
 * </ol>
 * <p>
 * 集群安全：通过 {@link DistributedLock} 以 mediaIdentification 为粒度加锁，
 * 保证同一时刻只有一个节点对单个流媒体服务器执行对账。
 * <p>
 * 宽限期：基于 {@link SsrcTransaction#getCreatedAt()} 判断，默认 {@value #GRACE_MILLIS}
 * 毫秒以内创建的事务即便 ssrc 不在匹配集合中也跳过（防止 INVITE 协商阶段误判）。
 * <p>
 * <b>架构说明</b>：本类放在 biz 层，只依赖 Redis 操作（{@link SsrcPoolManager} /
 * {@link SsrcTransactionManager}）和业务 Service，<u>不依赖 SIP 协议栈</u>，
 * 可被 iot-executor 进程（XXL-Job）和 video-server 进程共同注入。
 * <p>
 * 紧急重置（resetAndReinit）逻辑涉及重新初始化 SSRC 池（需要 SIP domain 配置），
 * 由 Controller 层组合调用 biz 层的清理 + 协议层的 {@code SsrcPoolService.initPool} 完成。
 *
 * @author mqttsnet
 * @since 2026-04-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SsrcPoolReconcileService {

    /** 事务创建后宽限期，不作为孤儿判定（毫秒） */
    private static final long GRACE_MILLIS = 60_000L;

    private final SsrcPoolManager ssrcPoolManager;
    private final SsrcTransactionManager ssrcTransactionManager;
    private final VideoMediaServerService videoMediaServerService;
    private final DistributedLock distributedLock;

    /**
     * 对指定流媒体服务器执行一次对账。
     *
     * @param mediaIdentification 流媒体服务器标识
     * @return 对账统计信息（已分配、匹配、释放数量）
     */
    public ReconcileResult reconcile(String mediaIdentification) {
        CacheKey lockKey = VideoLockKeyBuilder.forSsrcReconcile(mediaIdentification);
        var lockResult = distributedLock.tryLockAndRun(
                lockKey.getKey(),
                lockKey.getExpire().getSeconds(),
                TimeUnit.SECONDS,
                () -> doReconcile(mediaIdentification)
        );
        if (!lockResult.isLocked()) {
            log.debug("[SSRC对账] 其他节点正在执行: mediaIdentification={}", mediaIdentification);
            return ReconcileResult.skipped(mediaIdentification);
        }
        return lockResult.getResult() == null ? ReconcileResult.skipped(mediaIdentification) : lockResult.getResult();
    }

    /**
     * 按租户对账：扫描该租户的所有流媒体服务器并逐个执行孤儿 SSRC 回收。
     * <p>
     * 由 {@code VideoJobHandlerFacade} 通过 XXL-Job 按租户触发调用，进入前已设置
     * {@code ContextUtil.setTenantId(tenantId)}（本方法内部再兜底设置一次）。
     * <p>
     * 多租户数据源：视频相关表位于租户数据源 (@DS(BASE_TENANT))，必须有租户上下文
     * 才能正确路由到对应租户的业务库；否则会落到默认库导致 "表不存在" 异常。
     *
     * @param tenantId 租户ID
     */
    public void reconcileForTenant(Long tenantId) {
        if (tenantId == null) {
            log.warn("[SSRC对账] tenantId 为空，跳过");
            return;
        }
        try {
            ContextUtil.setTenantId(tenantId);
            List<VideoMediaServerResultDTO> servers =
                    videoMediaServerService.getVideoMediaServerResultDTOList(new VideoMediaServerPageQuery());
            if (CollUtil.isEmpty(servers)) {
                log.debug("[SSRC对账] 租户 {} 没有流媒体服务器，跳过", tenantId);
                return;
            }
            for (VideoMediaServerResultDTO server : servers) {
                String mediaId = server.getMediaIdentification();
                if (mediaId == null || mediaId.isBlank()) {
                    continue;
                }
                try {
                    reconcile(mediaId);
                } catch (Exception e) {
                    log.error("[SSRC对账] 单服务器对账异常: tenantId={}, mediaIdentification={}, error={}",
                            tenantId, mediaId, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("[SSRC对账] 租户对账失败: tenantId={}, error={}", tenantId, e.getMessage(), e);
        }
    }

    // ---------------- 内部实现 ----------------

    private ReconcileResult doReconcile(String mediaIdentification) {
        Map<String, String> allocations = ssrcPoolManager.getAllAllocations(mediaIdentification);
        if (allocations.isEmpty()) {
            return ReconcileResult.of(mediaIdentification, 0, 0, 0);
        }

        // 按设备聚合 SSRC
        Map<String, Set<String>> allocatedByDevice = new HashMap<>();
        for (Map.Entry<String, String> entry : allocations.entrySet()) {
            String deviceId = parseDeviceId(entry.getValue());
            if (deviceId != null && !deviceId.isBlank()) {
                allocatedByDevice.computeIfAbsent(deviceId, k -> new HashSet<>()).add(entry.getKey());
            }
        }

        int orphanCount = 0;
        // 遍历每个设备，批量比对其 SsrcTransaction 的 ssrc 集合
        for (Map.Entry<String, Set<String>> entry : allocatedByDevice.entrySet()) {
            String deviceId = entry.getKey();
            Set<String> allocatedSsrcs = entry.getValue();

            List<SsrcTransaction> transactions = ssrcTransactionManager.getAll(deviceId);
            Set<String> validSsrcs = extractValidSsrcs(transactions);
            // 若近 60s 内该设备有任一新创建的事务，视为"正在 INVITE 中"，整批跳过
            boolean hasRecentTransaction = transactions.stream()
                    .anyMatch(t -> t.ageMillis() >= 0 && t.ageMillis() < GRACE_MILLIS);

            for (String ssrc : allocatedSsrcs) {
                if (validSsrcs.contains(ssrc)) {
                    continue;
                }
                if (hasRecentTransaction) {
                    // 设备处于繁忙期，保守起见整批放过
                    log.debug("[SSRC对账] 设备宽限期内，跳过: deviceIdentification={}, ssrc={}", deviceId, ssrc);
                    continue;
                }
                log.warn("[SSRC对账] 发现孤儿 SSRC 并释放: mediaIdentification={}, deviceIdentification={}, ssrc={}",
                        mediaIdentification, deviceId, ssrc);
                try {
                    // 直接通过 Redis 释放，不走协议层的 SsrcPoolService（避免触发 event）
                    ssrcPoolManager.release(mediaIdentification, ssrc);
                    orphanCount++;
                } catch (Exception e) {
                    log.error("[SSRC对账] 释放孤儿 SSRC 失败: ssrc={}, error={}", ssrc, e.getMessage());
                }
            }
        }

        int allocatedCount = allocations.size();
        int matchedCount = allocatedCount - orphanCount;
        if (orphanCount > 0) {
            log.info("[SSRC对账] 完成: mediaIdentification={}, 总分配={}, 匹配={}, 已释放孤儿={}",
                    mediaIdentification, allocatedCount, matchedCount, orphanCount);
        }
        return ReconcileResult.of(mediaIdentification, allocatedCount, matchedCount, orphanCount);
    }

    private static Set<String> extractValidSsrcs(Collection<SsrcTransaction> transactions) {
        if (CollUtil.isEmpty(transactions)) {
            return Set.of();
        }
        return transactions.stream()
                .map(SsrcTransaction::getSsrc)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toSet());
    }

    /**
     * 从 usedBy 字符串（格式 "deviceIdentification:channelIdentification"）中解析 deviceIdentification。
     */
    static String parseDeviceId(String usedBy) {
        if (usedBy == null || usedBy.isBlank()) {
            return null;
        }
        int idx = usedBy.indexOf(':');
        return idx <= 0 ? usedBy : usedBy.substring(0, idx);
    }

    /** 对账结果 */
    public record ReconcileResult(
            String mediaIdentification,
            int allocatedCount,
            int matchedCount,
            int orphanReleasedCount,
            boolean skipped
    ) {
        public static ReconcileResult of(String mediaId, int allocated, int matched, int orphans) {
            return new ReconcileResult(mediaId, allocated, matched, orphans, false);
        }

        public static ReconcileResult skipped(String mediaId) {
            return new ReconcileResult(mediaId, 0, 0, 0, true);
        }
    }
}
