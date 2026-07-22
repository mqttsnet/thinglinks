package com.mqttsnet.thinglinks.video.service.stream;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.gb28181.event.subscribe.SipSubscribe;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.SsrcTransaction;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionTypeEnum;
import com.mqttsnet.thinglinks.video.enumeration.stream.SsrcPrefixEnum;
import com.mqttsnet.thinglinks.video.enumeration.stream.StreamModeEnum;
import com.mqttsnet.thinglinks.video.gb28181.cmd.PlayCommander;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.PlaybackEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.StreamEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.PlaybackControlEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.PlaybackRequestedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.StreamClosedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.session.InviteSessionService;
import com.mqttsnet.thinglinks.video.gb28181.session.RtpPortService;
import com.mqttsnet.thinglinks.video.gb28181.session.SsrcPoolService;
import com.mqttsnet.thinglinks.video.gb28181.session.StreamInfoService;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.media.common.MediaNodeService;
import com.mqttsnet.thinglinks.video.media.common.MediaNodeServiceFactory;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import javax.sip.ResponseEvent;
import javax.sip.SipFactory;
import javax.sip.header.CallIdHeader;
import javax.sip.header.HeaderFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Description:
 * 录像回放核心业务服务。
 * 实现设备端录像回放流程，支持倍速/拖拽/暂停/恢复控制。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class PlaybackService {

    private final VideoDeviceService videoDeviceService;
    private final VideoMediaServerService videoMediaServerService;
    private final MediaNodeServiceFactory mediaNodeServiceFactory;
    private final SsrcPoolService ssrcPoolService;
    private final RtpPortService rtpPortService;
    private final InviteSessionService inviteSessionService;
    private final StreamInfoService streamInfoService;
    private final PlayCommander playCommander;
    private final SIPSender sipSender;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final StreamEventPublisher streamEventPublisher;
    private final PlaybackEventPublisher playbackEventPublisher;

    private static final String DEFAULT_APP = "rtp";

    /**
     * PLAYBACK INVITE 响应超时（毫秒）。
     * GB28181 回放 INVITE 包含时间区间的录像查询，设备可能需要额外时间检查磁盘，预留到 15s。
     */
    private static final long PLAYBACK_INVITE_TIMEOUT_MS = 15_000L;

    /**
     * 发起录像回放
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @param startTime 回放开始时间
     * @param endTime   回放结束时间
     * @return 流信息
     */
    public StreamInfo playback(String deviceIdentification, String channelIdentification,
                                LocalDateTime startTime, LocalDateTime endTime) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");
        ArgumentAssert.notNull(startTime, "开始时间不能为空");
        ArgumentAssert.notNull(endTime, "结束时间不能为空");

        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) {
            throw BizException.wrap("设备不存在: " + deviceIdentification);
        }
        if (!Boolean.TRUE.equals(device.getOnlineStatus())) {
            throw BizException.wrap("设备离线，无法回放: " + deviceIdentification);
        }

        // 拦下"设备编号 == 平台 SIP 服务器编号"的自冲突配置（参见 PlayService.doPlay 同处注释）
        tenantSipConfigProvider.assertPlatformIdNotConflictWithDevice(deviceIdentification);

        VideoMediaServerResultDTO mediaServer = getMediaServer(device.getMediaIdentification());
        String mediaIdentification = mediaServer.getMediaIdentification();
        String ssrc = null;
        String streamId = null;
        int rtpPort = -1;

        try {
            ssrc = ssrcPoolService.allocateSsrc(mediaIdentification, SsrcPrefixEnum.PLAYBACK, deviceIdentification, channelIdentification);
            rtpPort = rtpPortService.allocatePort(mediaIdentification, deviceIdentification, channelIdentification);
            streamId = ssrc;
            String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";
            StreamModeEnum streamMode = StreamModeEnum.fromValue(
                    StrUtil.isNotBlank(device.getStreamMode()) ? device.getStreamMode() : "UDP"
            ).orElse(StreamModeEnum.UDP);

            MediaNodeService mediaNodeService = mediaNodeServiceFactory.getServiceByType(mediaServer.getType());
            var mediaServerEntity = convertToEntity(mediaServer);
            int tcpMode = switch (streamMode) {
                case TCP_PASSIVE -> 1;
                case TCP_ACTIVE -> 2;
                default -> 0;
            };
            int actualPort = mediaNodeService.createRtpServer(mediaServerEntity, streamId, ssrc, rtpPort, false, false, tcpMode);
            if (actualPort == -1) {
                throw BizException.wrap("创建RTP接收服务器失败");
            }

            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();
            CallIdHeader callId = sipSender.getNewCallIdHeader(tenantConfig.getEffectiveHost(), transport);
            String sdpIp = StrUtil.isNotBlank(mediaServer.getSdpHost()) ? mediaServer.getSdpHost() : mediaServer.getHost();

            playbackEventPublisher.publishPlaybackRequestedEvent(PlaybackRequestedEventSource.builder()
                    .deviceIdentification(deviceIdentification).channelIdentification(channelIdentification).mediaIdentification(mediaIdentification)
                    .ssrc(ssrc).startTime(startTime).endTime(endTime).build());

            // 异步 INVITE + 等待设备 200 OK；非 2xx / 超时统一走外层 catch 释放资源
            CompletableFuture<SipSubscribe.EventResult<?>> inviteFuture = playCommander.playbackInvite(
                    deviceIdentification, channelIdentification, device.getHost(), device.getPort(),
                    transport, streamMode, sdpIp, actualPort, ssrc, callId,
                    startTime, endTime, PLAYBACK_INVITE_TIMEOUT_MS);
            SipSubscribe.EventResult<?> inviteResult = awaitInviteResponse(inviteFuture,
                    deviceIdentification, channelIdentification);

            // 提取 Dialog tags + CSeq（BYE/INFO 必须复用）。SIP 响应头按 RFC 强制必选，直接取。
            ResponseEvent responseEvent = (ResponseEvent) inviteResult.event;
            SIPResponse response = (SIPResponse) responseEvent.getResponse();
            String fromTag = response.getFromHeader().getTag();
            String toTag = response.getToHeader().getTag();
            long inviteCseq = response.getCSeqHeader().getSeqNumber();

            SsrcTransaction transaction = new SsrcTransaction();
            transaction.setDeviceIdentification(deviceIdentification);
            transaction.setChannelIdentification(channelIdentification);
            transaction.setCallId(callId.getCallId());
            transaction.setApp(DEFAULT_APP);
            transaction.setStream(streamId);
            transaction.setMediaIdentification(mediaIdentification);
            transaction.setSsrc(ssrc);
            transaction.setRtpPort(actualPort);
            transaction.setType(InviteSessionTypeEnum.PLAYBACK);
            transaction.setFromTag(fromTag);
            transaction.setToTag(toTag);
            transaction.setCseq(inviteCseq);
            inviteSessionService.createSession(transaction);

            StreamInfo streamInfo = streamInfoService.buildAndCacheStreamInfo(
                    mediaServer, deviceIdentification, channelIdentification, InviteSessionTypeEnum.PLAYBACK.getValue(),
                    DEFAULT_APP, streamId, callId.getCallId());
            streamInfo.setStartTime(startTime.toString());
            streamInfo.setEndTime(endTime.toString());

            log.info("录像回放建立成功: deviceIdentification={}, channelIdentification={}, time={} ~ {}", deviceIdentification, channelIdentification, startTime, endTime);
            return streamInfo;

        } catch (Exception e) {
            // 先关闭 RTP 服务器
            if (StrUtil.isNotBlank(streamId)) {
                try {
                    MediaNodeService mediaNodeService = mediaNodeServiceFactory.getServiceByType(mediaServer.getType());
                    mediaNodeService.closeRtpServer(convertToEntity(mediaServer), streamId);
                } catch (Exception ex) {
                    log.error("关闭RTP服务器失败: streamId={}, error={}", streamId, ex.getMessage());
                }
            }
            if (StrUtil.isNotBlank(ssrc)) {
                try { ssrcPoolService.releaseSsrc(mediaIdentification, ssrc); }
                catch (Exception ex) { log.warn("释放SSRC失败: ssrc={}, error={}", ssrc, ex.getMessage()); }
            }
            if (rtpPort > 0) {
                try { rtpPortService.releasePort(mediaIdentification, rtpPort); }
                catch (Exception ex) { log.warn("释放RTP端口失败: port={}, error={}", rtpPort, ex.getMessage()); }
            }
            if (e instanceof BizException) { throw e; }
            throw BizException.wrap("发起录像回放失败: " + e.getMessage());
        }
    }

    /**
     * 停止回放
     */
    public void stopPlayback(String deviceIdentification, String channelIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        // 按 channelId 过滤，避免多通道/多回放并发时误停
        List<SsrcTransaction> transactions = inviteSessionService.getAllTransactions(deviceIdentification);
        SsrcTransaction pbTransaction = transactions.stream()
                .filter(t -> InviteSessionTypeEnum.PLAYBACK.equals(t.getType()))
                .filter(t -> channelIdentification.equals(t.getChannelIdentification()))
                .findFirst().orElse(null);

        if (pbTransaction == null) {
            streamInfoService.removeStreamInfo(deviceIdentification, channelIdentification, InviteSessionTypeEnum.PLAYBACK.getValue());
            return;
        }

        try {
            VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
            if (device != null) {
                String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";
                CallIdHeader callId = buildCallIdHeader(pbTransaction.getCallId());
                long nextCseq = pbTransaction.getCseq() + 1L;
                playCommander.bye(deviceIdentification, channelIdentification,
                        device.getHost(), device.getPort(), transport, callId,
                        pbTransaction.getFromTag(), pbTransaction.getToTag(), nextCseq);
            }
        } catch (Exception e) {
            log.error("发送BYE失败（本地仍清理资源）: {}", e.getMessage());
        }

        if (StrUtil.isNotBlank(pbTransaction.getMediaIdentification()) && StrUtil.isNotBlank(pbTransaction.getStream())) {
            try {
                VideoMediaServerResultDTO ms = videoMediaServerService.getVideoMediaServerResultDTO(pbTransaction.getMediaIdentification());
                if (ms != null) {
                    mediaNodeServiceFactory.getServiceByType(ms.getType()).closeRtpServer(convertToEntity(ms), pbTransaction.getStream());
                }
            } catch (Exception e) { log.error("关闭RTP服务器失败: {}", e.getMessage()); }
        }

        inviteSessionService.closeSession(deviceIdentification, pbTransaction.getCallId(), "用户停止回放");
        streamInfoService.removeStreamInfo(deviceIdentification, channelIdentification, InviteSessionTypeEnum.PLAYBACK.getValue());

        streamEventPublisher.publishStreamClosedEvent(StreamClosedEventSource.builder()
                .deviceIdentification(deviceIdentification).channelIdentification(channelIdentification).mediaIdentification(pbTransaction.getMediaIdentification())
                .app(pbTransaction.getApp()).stream(pbTransaction.getStream())
                .callId(pbTransaction.getCallId()).closeReason("用户停止回放")
                .userInitiated(true).build());

        log.info("停止回放: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
    }

    /**
     * 回放暂停（in-dialog INFO）
     */
    public void pause(String deviceIdentification, String channelIdentification) {
        doPlaybackControl(deviceIdentification, channelIdentification, "PAUSE", (transaction, ctx) ->
                playCommander.playbackPause(deviceIdentification, channelIdentification,
                        ctx.deviceHost, ctx.devicePort, ctx.transport, ctx.callIdHeader,
                        transaction.getFromTag(), transaction.getToTag(), ctx.nextCseq));
    }

    /**
     * 回放恢复（in-dialog INFO）
     */
    public void resume(String deviceIdentification, String channelIdentification) {
        doPlaybackControl(deviceIdentification, channelIdentification, "RESUME", (transaction, ctx) ->
                playCommander.playbackResume(deviceIdentification, channelIdentification,
                        ctx.deviceHost, ctx.devicePort, ctx.transport, ctx.callIdHeader,
                        transaction.getFromTag(), transaction.getToTag(), ctx.nextCseq));
    }

    /**
     * 回放倍速（in-dialog INFO）
     */
    public void speed(String deviceIdentification, String channelIdentification, double speed) {
        doPlaybackControl(deviceIdentification, channelIdentification, "SPEED", (transaction, ctx) ->
                playCommander.playbackSpeed(deviceIdentification, channelIdentification,
                        ctx.deviceHost, ctx.devicePort, ctx.transport, ctx.callIdHeader, speed,
                        transaction.getFromTag(), transaction.getToTag(), ctx.nextCseq));
    }

    /**
     * 回放拖拽（in-dialog INFO）
     */
    public void seek(String deviceIdentification, String channelIdentification, long seekTime) {
        doPlaybackControl(deviceIdentification, channelIdentification, "SEEK", (transaction, ctx) ->
                playCommander.playbackSeek(deviceIdentification, channelIdentification,
                        ctx.deviceHost, ctx.devicePort, ctx.transport, ctx.callIdHeader, seekTime,
                        transaction.getFromTag(), transaction.getToTag(), ctx.nextCseq));
    }

    /**
     * 回放控制通用分发：按 channelId 过滤会话、递增 CSeq、持久化，再调对应 INFO 命令。
     */
    private void doPlaybackControl(String deviceIdentification, String channelIdentification, String controlType, PlaybackAction action) {
        List<SsrcTransaction> transactions = inviteSessionService.getAllTransactions(deviceIdentification);
        SsrcTransaction pbTransaction = transactions.stream()
                .filter(t -> InviteSessionTypeEnum.PLAYBACK.equals(t.getType()))
                .filter(t -> channelIdentification.equals(t.getChannelIdentification()))
                .findFirst()
                .orElseThrow(() -> BizException.wrap("未找到回放会话: deviceIdentification=" + deviceIdentification
                        + ", channelIdentification=" + channelIdentification));

        try {
            VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
            if (device == null) {
                throw BizException.wrap("设备不存在: " + deviceIdentification);
            }
            PlaybackContext ctx = new PlaybackContext();
            ctx.deviceHost = device.getHost();
            ctx.devicePort = device.getPort();
            ctx.transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";
            ctx.callIdHeader = buildCallIdHeader(pbTransaction.getCallId());
            ctx.nextCseq = pbTransaction.getCseq() + 1L;

            action.execute(pbTransaction, ctx);

            // CSeq 自增并持久化，下次控制/BYE 正确递增
            pbTransaction.setCseq(ctx.nextCseq);
            inviteSessionService.createSession(pbTransaction);

            playbackEventPublisher.publishPlaybackControlEvent(PlaybackControlEventSource.builder()
                    .deviceIdentification(deviceIdentification).channelIdentification(channelIdentification)
                    .callId(pbTransaction.getCallId()).controlType(controlType).build());
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw BizException.wrap("回放控制(" + controlType + ")失败: " + e.getMessage());
        }
    }

    private static class PlaybackContext {
        String deviceHost;
        int devicePort;
        String transport;
        CallIdHeader callIdHeader;
        long nextCseq;
    }

    @FunctionalInterface
    private interface PlaybackAction {
        void execute(SsrcTransaction transaction, PlaybackContext ctx) throws Exception;
    }

    private CallIdHeader buildCallIdHeader(String callIdStr) throws Exception {
        HeaderFactory headerFactory = SipFactory.getInstance().createHeaderFactory();
        return headerFactory.createCallIdHeader(callIdStr);
    }

    /**
     * 同步等待 INVITE 响应（与 PlayService.awaitInviteResponse 逻辑一致，单独定义避免跨 Service 依赖）。
     */
    private SipSubscribe.EventResult<?> awaitInviteResponse(CompletableFuture<SipSubscribe.EventResult<?>> inviteFuture,
                                                            String deviceIdentification, String channelIdentification) {
        SipSubscribe.EventResult<?> result;
        try {
            result = inviteFuture.get(PLAYBACK_INVITE_TIMEOUT_MS + 1_000L, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw BizException.wrap("设备未应答回放 INVITE（超时 " + PLAYBACK_INVITE_TIMEOUT_MS + "ms）");
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw BizException.wrap("等待回放 INVITE 响应异常: " + e.getMessage());
        }
        if (result == null) throw BizException.wrap("设备未应答回放 INVITE");
        if (result.type == SipSubscribe.EventResultType.timeout) {
            throw BizException.wrap("设备未应答回放 INVITE（SIP 层超时）");
        }
        int statusCode = result.statusCode;
        if (statusCode >= 200 && statusCode < 300) {
            log.info("[回放INVITE响应] 设备: {}, 通道: {}, 状态码: {} (成功)",
                    deviceIdentification, channelIdentification, statusCode);
            return result;
        }
        String msg = StrUtil.isNotBlank(result.msg) ? result.msg : "无响应描述";
        throw BizException.wrap("设备拒绝回放 INVITE: statusCode=" + statusCode + ", reason=" + msg);
    }

    private VideoMediaServerResultDTO getMediaServer(String mediaIdentification) {
        VideoMediaServerResultDTO mediaServer;
        if (StrUtil.isNotBlank(mediaIdentification)) {
            mediaServer = videoMediaServerService.getVideoMediaServerResultDTO(mediaIdentification);
        } else {
            var servers = videoMediaServerService.getVideoMediaServerResultDTOList(null);
            mediaServer = servers.stream()
                    .filter(s -> Boolean.TRUE.equals(s.getOnlineStatus()))
                    .findFirst().orElse(null);
        }
        if (mediaServer == null) { throw BizException.wrap("无可用的流媒体服务器"); }
        if (!Boolean.TRUE.equals(mediaServer.getOnlineStatus())) {
            throw BizException.wrap("流媒体服务器离线: " + mediaServer.getMediaIdentification());
        }
        return mediaServer;
    }

    private com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer convertToEntity(VideoMediaServerResultDTO dto) {
        return VideoMediaServerConverter.toEntity(dto);
    }
}
