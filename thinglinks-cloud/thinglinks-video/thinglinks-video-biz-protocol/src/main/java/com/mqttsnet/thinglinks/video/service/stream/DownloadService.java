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
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.StreamEventPublisher;
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
 * 录像下载核心业务服务。
 * 实现设备端录像下载流程，支持倍速下载和进度跟踪。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class DownloadService {

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

    private static final String DEFAULT_APP = "rtp";
    private static final int DEFAULT_DOWNLOAD_SPEED = 4;

    /** 下载 INVITE 响应超时（毫秒）。下载速率可达 4x/8x，设备准备时间可能更长 */
    private static final long DOWNLOAD_INVITE_TIMEOUT_MS = 15_000L;

    /**
     * 发起录像下载
     *
     * @param deviceIdentification      设备国标编号
     * @param channelIdentification     通道国标编号
     * @param startTime     下载开始时间
     * @param endTime       下载结束时间
     * @param downloadSpeed 下载倍速
     * @return 流信息
     */
    public StreamInfo download(String deviceIdentification, String channelIdentification,
                                LocalDateTime startTime, LocalDateTime endTime,
                                Integer downloadSpeed) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");
        ArgumentAssert.notNull(startTime, "开始时间不能为空");
        ArgumentAssert.notNull(endTime, "结束时间不能为空");

        int speed = (downloadSpeed != null && downloadSpeed > 0) ? downloadSpeed : DEFAULT_DOWNLOAD_SPEED;

        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) { throw BizException.wrap("设备不存在: " + deviceIdentification); }
        if (!Boolean.TRUE.equals(device.getOnlineStatus())) { throw BizException.wrap("设备离线: " + deviceIdentification); }

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
            var entity = convertToEntity(mediaServer);
            int tcpMode = switch (streamMode) {
                case TCP_PASSIVE -> 1;
                case TCP_ACTIVE -> 2;
                default -> 0;
            };
            int actualPort = mediaNodeService.createRtpServer(entity, streamId, ssrc, rtpPort, false, false, tcpMode);
            if (actualPort == -1) { throw BizException.wrap("创建RTP接收服务器失败"); }

            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();
            CallIdHeader callId = sipSender.getNewCallIdHeader(tenantConfig.getEffectiveHost(), transport);
            String sdpIp = StrUtil.isNotBlank(mediaServer.getSdpHost()) ? mediaServer.getSdpHost() : mediaServer.getHost();

            CompletableFuture<SipSubscribe.EventResult<?>> inviteFuture = playCommander.downloadInvite(
                    deviceIdentification, channelIdentification, device.getHost(), device.getPort(),
                    transport, streamMode, sdpIp, actualPort, ssrc, callId,
                    startTime, endTime, speed, DOWNLOAD_INVITE_TIMEOUT_MS);
            SipSubscribe.EventResult<?> inviteResult = awaitInviteResponse(inviteFuture, deviceIdentification, channelIdentification);

            // 提取 Dialog tags + CSeq。SIP 响应头按 RFC 强制必选，直接取。
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
            transaction.setType(InviteSessionTypeEnum.DOWNLOAD);
            transaction.setFromTag(fromTag);
            transaction.setToTag(toTag);
            transaction.setCseq(inviteCseq);
            inviteSessionService.createSession(transaction);

            StreamInfo streamInfo = streamInfoService.buildAndCacheStreamInfo(
                    mediaServer, deviceIdentification, channelIdentification, InviteSessionTypeEnum.DOWNLOAD.getValue(),
                    DEFAULT_APP, streamId, callId.getCallId());
            streamInfo.setStartTime(startTime.toString());
            streamInfo.setEndTime(endTime.toString());

            log.info("录像下载INVITE发送成功: deviceIdentification={}, channelIdentification={}, speed={}x", deviceIdentification, channelIdentification, speed);
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
            throw BizException.wrap("发起录像下载失败: " + e.getMessage());
        }
    }

    /**
     * 停止下载
     */
    public void stopDownload(String deviceIdentification, String channelIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        // 按 channelId 过滤
        List<SsrcTransaction> transactions = inviteSessionService.getAllTransactions(deviceIdentification);
        SsrcTransaction dlTransaction = transactions.stream()
                .filter(t -> InviteSessionTypeEnum.DOWNLOAD.equals(t.getType()))
                .filter(t -> channelIdentification.equals(t.getChannelIdentification()))
                .findFirst().orElse(null);

        if (dlTransaction == null) {
            streamInfoService.removeStreamInfo(deviceIdentification, channelIdentification, InviteSessionTypeEnum.DOWNLOAD.getValue());
            return;
        }

        try {
            VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
            if (device != null) {
                String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";
                CallIdHeader callId = buildCallIdHeader(dlTransaction.getCallId());
                long nextCseq = dlTransaction.getCseq() + 1L;
                playCommander.bye(deviceIdentification, channelIdentification,
                        device.getHost(), device.getPort(), transport, callId,
                        dlTransaction.getFromTag(), dlTransaction.getToTag(), nextCseq);
            }
        } catch (Exception e) { log.error("发送BYE失败（本地仍清理资源）: {}", e.getMessage()); }

        if (StrUtil.isNotBlank(dlTransaction.getMediaIdentification()) && StrUtil.isNotBlank(dlTransaction.getStream())) {
            try {
                VideoMediaServerResultDTO ms = videoMediaServerService.getVideoMediaServerResultDTO(dlTransaction.getMediaIdentification());
                if (ms != null) {
                    mediaNodeServiceFactory.getServiceByType(ms.getType()).closeRtpServer(convertToEntity(ms), dlTransaction.getStream());
                }
            } catch (Exception e) { log.error("关闭RTP服务器失败: {}", e.getMessage()); }
        }

        inviteSessionService.closeSession(deviceIdentification, dlTransaction.getCallId(), "用户停止下载");
        streamInfoService.removeStreamInfo(deviceIdentification, channelIdentification, InviteSessionTypeEnum.DOWNLOAD.getValue());

        streamEventPublisher.publishStreamClosedEvent(StreamClosedEventSource.builder()
                .deviceIdentification(deviceIdentification).channelIdentification(channelIdentification).mediaIdentification(dlTransaction.getMediaIdentification())
                .app(dlTransaction.getApp()).stream(dlTransaction.getStream())
                .callId(dlTransaction.getCallId()).closeReason("用户停止下载")
                .userInitiated(true).build());

        log.info("停止下载: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
    }

    private VideoMediaServerResultDTO getMediaServer(String mediaIdentification) {
        VideoMediaServerResultDTO mediaServer;
        if (StrUtil.isNotBlank(mediaIdentification)) {
            mediaServer = videoMediaServerService.getVideoMediaServerResultDTO(mediaIdentification);
        } else {
            var servers = videoMediaServerService.getVideoMediaServerResultDTOList(null);
            mediaServer = servers.stream().filter(s -> Boolean.TRUE.equals(s.getOnlineStatus()))
                    .findFirst().orElse(null);
        }
        if (mediaServer == null) { throw BizException.wrap("无可用的流媒体服务器"); }
        if (!Boolean.TRUE.equals(mediaServer.getOnlineStatus())) {
            throw BizException.wrap("流媒体服务器离线: " + mediaServer.getMediaIdentification());
        }
        return mediaServer;
    }

    private CallIdHeader buildCallIdHeader(String callIdStr) throws Exception {
        HeaderFactory headerFactory = SipFactory.getInstance().createHeaderFactory();
        return headerFactory.createCallIdHeader(callIdStr);
    }

    private SipSubscribe.EventResult<?> awaitInviteResponse(CompletableFuture<SipSubscribe.EventResult<?>> inviteFuture,
                                                            String deviceIdentification, String channelIdentification) {
        SipSubscribe.EventResult<?> result;
        try {
            result = inviteFuture.get(DOWNLOAD_INVITE_TIMEOUT_MS + 1_000L, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw BizException.wrap("设备未应答下载 INVITE（超时 " + DOWNLOAD_INVITE_TIMEOUT_MS + "ms）");
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw BizException.wrap("等待下载 INVITE 响应异常: " + e.getMessage());
        }
        if (result == null) throw BizException.wrap("设备未应答下载 INVITE");
        if (result.type == SipSubscribe.EventResultType.timeout) {
            throw BizException.wrap("设备未应答下载 INVITE（SIP 层超时）");
        }
        int statusCode = result.statusCode;
        if (statusCode >= 200 && statusCode < 300) {
            log.info("[下载INVITE响应] 设备: {}, 通道: {}, 状态码: {} (成功)",
                    deviceIdentification, channelIdentification, statusCode);
            return result;
        }
        String msg = StrUtil.isNotBlank(result.msg) ? result.msg : "无响应描述";
        throw BizException.wrap("设备拒绝下载 INVITE: statusCode=" + statusCode + ", reason=" + msg);
    }

    private com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer convertToEntity(VideoMediaServerResultDTO dto) {
        return VideoMediaServerConverter.toEntity(dto);
    }
}
