package com.mqttsnet.thinglinks.video.gb28181.session;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.StreamEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.RtpPortAllocatedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.RtpPortReleasedEventSource;
import com.mqttsnet.thinglinks.video.manager.stream.RtpPortManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Description:
 * RTP 端口业务服务。
 * 在 {@link RtpPortManager} 基础上封装业务逻辑，包括：
 * - 从 rtpPortRange 配置解析端口范围并初始化端口池
 * - 端口分配/释放时发布事件通知
 * - 端口耗尽时抛出业务异常
 * <p>
 * RTP 端口必须为偶数（RTCP = RTP + 1）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class RtpPortService {

    private final RtpPortManager rtpPortManager;
    private final StreamEventPublisher streamEventPublisher;

    /**
     * 根据端口范围字符串初始化 RTP 端口池
     *
     * @param mediaIdentification 流媒体服务器标识
     * @param rtpPortRange  端口范围（格式：startPort,endPort，如 "30000,30500"）
     */
    public void initPool(String mediaIdentification, String rtpPortRange) {
        if (StrUtil.isBlank(rtpPortRange)) {
            log.warn("RTP端口范围未配置: mediaIdentification={}", mediaIdentification);
            return;
        }

        String[] parts = rtpPortRange.split(",");
        if (parts.length != 2) {
            throw BizException.wrap("RTP端口范围格式错误，应为 startPort,endPort: " + rtpPortRange);
        }

        Integer startPort = NumberUtil.parseInt(parts[0].trim(), null);
        Integer endPort = NumberUtil.parseInt(parts[1].trim(), null);
        if (startPort == null || endPort == null) {
            throw BizException.wrap("RTP端口范围包含非法数字: " + rtpPortRange);
        }

        if (startPort < 1024 || endPort > 65535 || startPort >= endPort) {
            throw BizException.wrap("RTP端口范围无效（需 1024 <= start < end <= 65535）: " + rtpPortRange);
        }

        // 仅使用偶数端口（RTCP = RTP + 1）
        Set<Integer> portSet = new LinkedHashSet<>();
        for (int port = startPort; port <= endPort; port += 2) {
            if (port % 2 != 0) {
                continue;
            }
            portSet.add(port);
        }

        if (portSet.isEmpty()) {
            throw BizException.wrap("RTP端口范围内无可用偶数端口: " + rtpPortRange);
        }

        rtpPortManager.initPool(mediaIdentification, portSet);
        log.info("RTP端口池初始化: mediaIdentification={}, range={}, 偶数端口数={}", mediaIdentification, rtpPortRange, portSet.size());
    }

    /**
     * 默认 RTP 端口范围（ZLM / 大部分 GB28181 流媒体服务器的标准默认范围）。
     * 仅在 {@code VideoMediaServer.rtpPortRange} 字段为空时兜底使用。
     */
    private static final String DEFAULT_RTP_PORT_RANGE = "30000,30500";

    /**
     * 确保 RTP 端口池已初始化：池为空时自动 {@link #initPool} 补齐。
     * <p>支持两种入参来源：
     * <ol>
     *   <li>{@code rtpPortRange} 非空 → 按传入值初始化（优先，通常来自 {@code VideoMediaServer.rtpPortRange}）</li>
     *   <li>{@code rtpPortRange} 为空 → 用 {@link #DEFAULT_RTP_PORT_RANGE} 兜底，并 log.warn 建议运维补齐配置</li>
     * </ol>
     *
     * @param mediaIdentification 流媒体服务器标识
     * @param rtpPortRange        端口范围，格式 "30000,30500"；可为 null/blank（会走默认值）
     */
    public void ensurePoolInitialized(String mediaIdentification, String rtpPortRange) {
        int poolSize = rtpPortManager.getPoolSize(mediaIdentification);
        if (poolSize != 0) {
            return;
        }
        String effectiveRange;
        String source;
        if (StrUtil.isNotBlank(rtpPortRange)) {
            effectiveRange = rtpPortRange;
            source = "VideoMediaServer.rtpPortRange";
        } else {
            effectiveRange = DEFAULT_RTP_PORT_RANGE;
            source = "default";
            log.warn("[RTP端口池] 流媒体服务器 {} 未配置 rtpPortRange，使用默认值 {}，建议在 video_media_server 表补齐",
                    mediaIdentification, DEFAULT_RTP_PORT_RANGE);
        }
        log.warn("[RTP端口池] 首次发现池未初始化，自动补建: mediaIdentification={}, range={}, source={}",
                mediaIdentification, effectiveRange, source);
        try {
            initPool(mediaIdentification, effectiveRange);
            int newSize = rtpPortManager.getPoolSize(mediaIdentification);
            log.info("[RTP端口池] 自动补建完成: mediaIdentification={}, poolSize={}", mediaIdentification, newSize);
            if (newSize == 0) {
                throw BizException.wrap("RTP端口池自动补建后仍为空: " + mediaIdentification);
            }
        } catch (Exception e) {
            log.error("[RTP端口池] 自动补建失败: mediaIdentification={}, range={}, error={}",
                    mediaIdentification, effectiveRange, e.getMessage(), e);
            throw e instanceof BizException ? (BizException) e
                    : BizException.wrap("RTP端口池自动补建失败: " + e.getMessage());
        }
    }

    /**
     * 分配一个 RTP 端口
     *
     * @param mediaIdentification 流媒体服务器标识
     * @param deviceIdentification      设备编号
     * @param channelIdentification     通道编号
     * @return 分配到的端口号
     * @throws BizException 端口池耗尽时抛出
     */
    public int allocatePort(String mediaIdentification, String deviceIdentification, String channelIdentification) {
        String usedBy = deviceIdentification + ":" + channelIdentification;
        int port = rtpPortManager.allocate(mediaIdentification, usedBy);

        if (port == -1) {
            throw BizException.wrap("RTP端口池已耗尽: mediaIdentification=" + mediaIdentification);
        }

        // 发布端口分配事件
        streamEventPublisher.publishRtpPortAllocatedEvent(RtpPortAllocatedEventSource.builder()
                .mediaIdentification(mediaIdentification)
                .port(port)
                .deviceIdentification(deviceIdentification)
                .channelIdentification(channelIdentification)
                .build());

        log.info("RTP端口分配: mediaIdentification={}, port={}, deviceIdentification={}, channelIdentification={}",
                mediaIdentification, port, deviceIdentification, channelIdentification);
        return port;
    }

    /**
     * 释放 RTP 端口
     *
     * @param mediaIdentification 流媒体服务器标识
     * @param port          端口号
     */
    public void releasePort(String mediaIdentification, int port) {
        if (port <= 0) {
            return;
        }
        rtpPortManager.release(mediaIdentification, port);

        // 发布端口释放事件
        streamEventPublisher.publishRtpPortReleasedEvent(RtpPortReleasedEventSource.builder()
                .mediaIdentification(mediaIdentification)
                .port(port)
                .build());

        log.info("RTP端口释放: mediaIdentification={}, port={}", mediaIdentification, port);
    }

    /**
     * 获取可用端口数量
     *
     * @param mediaIdentification 流媒体服务器标识
     * @return 可用端口数量
     */
    public int getAvailableCount(String mediaIdentification) {
        return rtpPortManager.getAvailableCount(mediaIdentification);
    }
}
