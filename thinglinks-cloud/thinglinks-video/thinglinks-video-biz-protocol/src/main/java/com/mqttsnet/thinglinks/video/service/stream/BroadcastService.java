package com.mqttsnet.thinglinks.video.service.stream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.SsrcTransaction;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionTypeEnum;
import com.mqttsnet.thinglinks.video.enumeration.stream.SsrcPrefixEnum;
import com.mqttsnet.thinglinks.video.enumeration.stream.StreamModeEnum;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.gb28181.cmd.BroadcastCommander;
import com.mqttsnet.thinglinks.video.gb28181.session.InviteSessionService;
import com.mqttsnet.thinglinks.video.gb28181.session.RtpPortService;
import com.mqttsnet.thinglinks.video.gb28181.session.SsrcPoolService;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.media.common.MediaNodeServiceFactory;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import javax.sip.header.CallIdHeader;
import java.util.List;

/**
 * Description:
 * 语音广播/对讲业务服务。
 * 实现语音广播通知→INVITE建立音频流→BYE结束广播的完整流程。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class BroadcastService {

    private final VideoDeviceService videoDeviceService;
    private final VideoMediaServerService videoMediaServerService;
    private final BroadcastCommander broadcastCommander;
    private final SsrcPoolService ssrcPoolService;
    private final RtpPortService rtpPortService;
    private final InviteSessionService inviteSessionService;
    private final MediaNodeServiceFactory mediaNodeServiceFactory;
    private final SIPSender sipSender;
    private final TenantSipConfigProvider tenantSipConfigProvider;

    private static final String BROADCAST_APP = "broadcast";

    /**
     * 发起语音广播
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     */
    public void startBroadcast(String deviceIdentification, String channelIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) {
            throw BizException.wrap("设备不存在: " + deviceIdentification);
        }
        if (!Boolean.TRUE.equals(device.getOnlineStatus())) {
            throw BizException.wrap("设备离线: " + deviceIdentification);
        }

        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        // 1. 发送广播通知（MESSAGE）
        broadcastCommander.broadcastNotify(deviceIdentification, channelIdentification, device.getHost(), device.getPort(), transport);
        log.info("语音广播通知已发送: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);

        // 2. 获取流媒体服务器
        VideoMediaServerResultDTO mediaServerDTO = getMediaServer(device);
        VideoMediaServer mediaServer = convertToEntity(mediaServerDTO);
        String mediaIdentification = mediaServerDTO.getMediaIdentification();

        // 3. 分配 SSRC 和 RTP 端口
        String ssrc = ssrcPoolService.allocateSsrc(mediaIdentification, SsrcPrefixEnum.PLAYBACK, deviceIdentification, channelIdentification);
        int rtpPort = rtpPortService.allocatePort(mediaIdentification, deviceIdentification, channelIdentification);
        String streamId = deviceIdentification + "_" + channelIdentification;

        try {
            // 4. 创建 RTP 收流服务
            var mediaNodeService = mediaNodeServiceFactory.getService(mediaServer);
            mediaNodeService.createRtpServer(mediaServer, streamId, ssrc, rtpPort, false, false, 0);

            // 5. 发送 INVITE（建立音频通道）
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();
            CallIdHeader callId = sipSender.getNewCallIdHeader(tenantConfig.getEffectiveHost(), transport);
            String sdpIp = StrUtil.isNotBlank(mediaServerDTO.getSdpHost()) ? mediaServerDTO.getSdpHost() : mediaServerDTO.getHost();
            broadcastCommander.broadcastInvite(
                    deviceIdentification, channelIdentification, device.getHost(), device.getPort(), transport,
                    StreamModeEnum.UDP, sdpIp, rtpPort, ssrc, callId);

            // 6. 创建会话事务
            SsrcTransaction transaction = new SsrcTransaction();
            transaction.setDeviceIdentification(deviceIdentification);
            transaction.setChannelIdentification(channelIdentification);
            transaction.setCallId(callId.getCallId());
            transaction.setApp(BROADCAST_APP);
            transaction.setStream(streamId);
            transaction.setMediaIdentification(mediaIdentification);
            transaction.setSsrc(ssrc);
            transaction.setType(InviteSessionTypeEnum.BROADCAST);
            inviteSessionService.createSession(transaction);

            log.info("语音广播INVITE已发送: deviceIdentification={}, channelIdentification={}, ssrc={}, port={}",
                    deviceIdentification, channelIdentification, ssrc, rtpPort);
        } catch (Exception e) {
            // 先关闭 RTP 服务器
            if (StrUtil.isNotBlank(streamId)) {
                try {
                    var cleanupService = mediaNodeServiceFactory.getService(mediaServer);
                    cleanupService.closeRtpServer(mediaServer, streamId);
                } catch (Exception ex) {
                    log.error("关闭RTP服务器失败: streamId={}, error={}", streamId, ex.getMessage());
                }
            }
            try { ssrcPoolService.releaseSsrc(mediaIdentification, ssrc); } catch (Exception ex) {
                log.error("释放SSRC失败: {}", ex.getMessage());
            }
            try { rtpPortService.releasePort(mediaIdentification, rtpPort); } catch (Exception ex) {
                log.error("释放RTP端口失败: {}", ex.getMessage());
            }
            log.error("语音广播失败: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification, e);
            if (e instanceof BizException) {
                throw e;
            }
            throw BizException.wrap("语音广播失败: " + e.getMessage());
        }
    }

    /**
     * 停止语音广播
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     */
    public void stopBroadcast(String deviceIdentification, String channelIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        // 按 channelId 过滤，避免多通道并发时误停
        List<SsrcTransaction> transactions = inviteSessionService.getAllTransactions(deviceIdentification);
        SsrcTransaction broadcastTransaction = transactions.stream()
                .filter(t -> InviteSessionTypeEnum.BROADCAST.equals(t.getType()))
                .filter(t -> channelIdentification.equals(t.getChannelIdentification()))
                .findFirst()
                .orElse(null);

        if (broadcastTransaction == null) {
            log.warn("未找到语音广播会话: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
            return;
        }

        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device != null) {
            String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";
            try {
                CallIdHeader callId = buildCallIdHeader(broadcastTransaction.getCallId());
                long nextCseq = broadcastTransaction.getCseq() + 1L;
                broadcastCommander.broadcastBye(deviceIdentification, channelIdentification,
                        device.getHost(), device.getPort(), transport, callId,
                        broadcastTransaction.getFromTag(), broadcastTransaction.getToTag(), nextCseq);
            } catch (Exception e) {
                log.error("发送广播BYE失败（本地仍清理资源）: deviceIdentification={}", deviceIdentification, e);
            }
        }

        inviteSessionService.closeSession(deviceIdentification, broadcastTransaction.getCallId(), "用户停止广播");
        log.info("停止语音广播: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
    }

    private CallIdHeader buildCallIdHeader(String callIdStr) throws Exception {
        javax.sip.header.HeaderFactory headerFactory = javax.sip.SipFactory.getInstance().createHeaderFactory();
        return headerFactory.createCallIdHeader(callIdStr);
    }

    private VideoMediaServerResultDTO getMediaServer(VideoDeviceResultVO device) {
        if (StrUtil.isNotBlank(device.getMediaIdentification())) {
            VideoMediaServerResultDTO server = videoMediaServerService.getVideoMediaServerResultDTO(device.getMediaIdentification());
            if (server != null) {
                return server;
            }
        }
        List<VideoMediaServerResultDTO> servers = videoMediaServerService.getVideoMediaServerResultDTOList(null);
        if (CollUtil.isEmpty(servers)) {
            throw BizException.wrap("无可用的流媒体服务器");
        }
        return servers.get(0);
    }

    private VideoMediaServer convertToEntity(VideoMediaServerResultDTO dto) {
        return VideoMediaServerConverter.toEntity(dto);
    }
}
