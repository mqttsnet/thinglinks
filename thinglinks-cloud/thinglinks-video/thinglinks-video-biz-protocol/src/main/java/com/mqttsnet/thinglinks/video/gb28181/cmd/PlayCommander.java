package com.mqttsnet.thinglinks.video.gb28181.cmd;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.enumeration.stream.StreamModeEnum;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.SipCommandEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SipCommandSentEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.subscribe.SipSubscribe;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Description:
 * 播放命令发送器。
 * 负责构建和发送实时点播（Play）、录像回放（Playback）、
 * 录像下载（Download）相关的 SIP INVITE/BYE/INFO 命令。
 * <p>
 * 所有命令发送后通过 Event 通知结果。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayCommander {

    private final SipMessageBuilder sipMessageBuilder;
    private final SdpBuilder sdpBuilder;
    private final SIPSender sipSender;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final SipCommandEventPublisher sipCommandEventPublisher;

    /**
     * 发起实时点播 INVITE
     *
     * @param deviceIdentification    设备国标编号
     * @param channelIdentification   通道国标编号
     * @param deviceIp    设备 IP
     * @param devicePort  设备 SIP 端口
     * @param transport   传输协议（UDP/TCP）
     * @param streamMode  流传输模式
     * @param mediaIp     流媒体服务器 IP
     * @param rtpPort     RTP 接收端口
     * @param ssrc        SSRC 值
     * @param callId      CallIdHeader
     * @return SIP INVITE 请求
     */
    /**
     * 发送实时点播 INVITE（异步，等待设备 200 OK 响应）。
     * <p>
     * 相比同步版本，这里用 {@link SIPSender#transmitRequestAsync} 在发送前就订阅响应，
     * 调用方通过返回的 {@link CompletableFuture} 阻塞等待 200 OK 或超时。这样能避免
     * "INVITE 发出即返回成功" 导致前端看到 URL 却拉不到流的问题（Phase 2-1）。
     *
     * @param timeoutMs 等待响应的超时毫秒数（null 使用 {@code sip.timeout} 配置）
     * @return 一个 Future，收到 2xx/4xx/5xx 响应或超时时 complete
     */
    public CompletableFuture<SipSubscribe.EventResult<?>> playInvite(String deviceIdentification, String channelIdentification,
                                                                    String deviceIp, int devicePort,
                                                                    String transport, StreamModeEnum streamMode,
                                                                    String mediaIp, int rtpPort, String ssrc,
                                                                    CallIdHeader callId, Long timeoutMs) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            // [诊断] 打印出口地址选择的完整字段链路，定位 Via/Contact/CallId 的 IP 是怎么选出来的
            log.info("[SIP出口诊断] deviceIdentification={}, tenantConfig.bindIp={}, tenantConfig.sipServerAddress={}, " +
                            "tenantConfig.host(扫描得到)={}, tenantConfig.effectiveHost(最终选择)={}, tenantConfig.sipId={}, tenantConfig.domain={}",
                    deviceIdentification, tenantConfig.getBindIp(), tenantConfig.getSipServerAddress(),
                    tenantConfig.getHost(), tenantConfig.getEffectiveHost(),
                    tenantConfig.getSipId(), tenantConfig.getDomain());

            String sdp = sdpBuilder.buildPlaySdp("Play", mediaIp, rtpPort, ssrc,
                    streamMode, deviceIdentification, channelIdentification);

            Request request = sipMessageBuilder.buildInviteRequest(
                    deviceIdentification, channelIdentification, deviceIp, devicePort,
                    sdp, transport, ssrc, callId, tenantConfig);

            // transmitRequestAsync 内部会：先 sipSubscribe.subscribe(callId+cseq) 再 doSend，
            // 避免响应到达快于订阅注册的竞态。
            CompletableFuture<SipSubscribe.EventResult<?>> future =
                    sipSender.transmitRequestAsync(tenantConfig.getEffectiveHost(), request, timeoutMs);

            log.info("发送实时点播INVITE(异步): deviceIdentification={}, channelIdentification={}, ssrc={}, callId={}, timeoutMs={}",
                    deviceIdentification, channelIdentification, ssrc, callId.getCallId(), timeoutMs);

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType("PLAY")
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(channelIdentification)
                            .callId(callId.getCallId())
                            .sipMethod("INVITE")
                            .build());

            return future;
        } catch (Exception e) {
            log.error("发送实时点播INVITE失败: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification, e);
            throw BizException.wrap("发送实时点播INVITE失败: " + e.getMessage());
        }
    }

    /**
     * 发起录像回放 INVITE
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param streamMode 流传输模式
     * @param mediaIp    流媒体服务器 IP
     * @param rtpPort    RTP 接收端口
     * @param ssrc       SSRC 值
     * @param callId     CallIdHeader
     * @param startTime  回放开始时间
     * @param endTime    回放结束时间
     * @return SIP INVITE 请求
     */
    public CompletableFuture<SipSubscribe.EventResult<?>> playbackInvite(
            String deviceIdentification, String channelIdentification,
            String deviceIp, int devicePort,
            String transport, StreamModeEnum streamMode,
            String mediaIp, int rtpPort, String ssrc,
            CallIdHeader callId,
            LocalDateTime startTime, LocalDateTime endTime,
            Long timeoutMs) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            String sdp = sdpBuilder.buildPlaybackSdp(mediaIp, rtpPort, ssrc,
                    streamMode, deviceIdentification, channelIdentification, startTime, endTime);

            Request request = sipMessageBuilder.buildInviteRequest(
                    deviceIdentification, channelIdentification, deviceIp, devicePort,
                    sdp, transport, ssrc, callId, tenantConfig);

            // 异步：等待 200 OK 后上层从 response 取 To-tag 存进 SsrcTransaction
            CompletableFuture<SipSubscribe.EventResult<?>> future =
                    sipSender.transmitRequestAsync(tenantConfig.getEffectiveHost(), request, timeoutMs);

            log.info("发送录像回放INVITE(异步): deviceIdentification={}, channelIdentification={}, ssrc={}, time={} ~ {}, timeoutMs={}",
                    deviceIdentification, channelIdentification, ssrc, startTime, endTime, timeoutMs);

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType("PLAYBACK")
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(channelIdentification)
                            .callId(callId.getCallId())
                            .sipMethod("INVITE")
                            .build());

            return future;
        } catch (Exception e) {
            log.error("发送录像回放INVITE失败: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification, e);
            throw BizException.wrap("发送录像回放INVITE失败: " + e.getMessage());
        }
    }

    /**
     * 发起录像下载 INVITE
     *
     * @param deviceIdentification    设备国标编号
     * @param channelIdentification   通道国标编号
     * @param deviceIp    设备 IP
     * @param devicePort  设备 SIP 端口
     * @param transport   传输协议
     * @param streamMode  流传输模式
     * @param mediaIp     流媒体服务器 IP
     * @param rtpPort     RTP 接收端口
     * @param ssrc        SSRC 值
     * @param callId      CallIdHeader
     * @param startTime   下载开始时间
     * @param endTime     下载结束时间
     * @param downloadSpeed 下载倍速
     * @return SIP INVITE 请求
     */
    public CompletableFuture<SipSubscribe.EventResult<?>> downloadInvite(
            String deviceIdentification, String channelIdentification,
            String deviceIp, int devicePort,
            String transport, StreamModeEnum streamMode,
            String mediaIp, int rtpPort, String ssrc,
            CallIdHeader callId,
            LocalDateTime startTime, LocalDateTime endTime,
            int downloadSpeed,
            Long timeoutMs) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            String sdp = sdpBuilder.buildDownloadSdp(mediaIp, rtpPort, ssrc,
                    streamMode, deviceIdentification, channelIdentification, startTime, endTime, downloadSpeed);

            Request request = sipMessageBuilder.buildInviteRequest(
                    deviceIdentification, channelIdentification, deviceIp, devicePort,
                    sdp, transport, ssrc, callId, tenantConfig);

            CompletableFuture<SipSubscribe.EventResult<?>> future =
                    sipSender.transmitRequestAsync(tenantConfig.getEffectiveHost(), request, timeoutMs);

            log.info("发送录像下载INVITE(异步): deviceIdentification={}, channelIdentification={}, ssrc={}, speed={}x, timeoutMs={}",
                    deviceIdentification, channelIdentification, ssrc, downloadSpeed, timeoutMs);

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType("DOWNLOAD")
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(channelIdentification)
                            .callId(callId.getCallId())
                            .sipMethod("INVITE")
                            .build());

            return future;
        } catch (Exception e) {
            log.error("发送录像下载INVITE失败: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification, e);
            throw BizException.wrap("发送录像下载INVITE失败: " + e.getMessage());
        }
    }

    /**
     * 发送 BYE 终止会话
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param callId     CallIdHeader
     */
    /**
     * 发送 BYE 终止会话（in-dialog）。
     *
     * @param fromTag INVITE 请求的 From-tag（UAC 生成，dialog 内不变）
     * @param toTag   INVITE 2xx 响应中的 To-tag（设备填入，dialog 内不变）
     * @param cseq    本次 BYE 使用的 CSeq（必须比 INVITE 大；一般取 SsrcTransaction.cseq + 1）
     */
    public void bye(String deviceIdentification, String channelIdentification,
                    String deviceIp, int devicePort,
                    String transport, CallIdHeader callId,
                    String fromTag, String toTag, long cseq) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            Request request = sipMessageBuilder.buildByeRequest(
                    deviceIdentification, channelIdentification, deviceIp, devicePort,
                    transport, callId, tenantConfig, fromTag, toTag, cseq);

            sipSender.transmitRequest(tenantConfig.getEffectiveHost(), request);

            log.info("发送BYE: deviceIdentification={}, channelIdentification={}, callId={}, fromTag={}, toTag={}, cseq={}",
                    deviceIdentification, channelIdentification, callId.getCallId(), fromTag, toTag, cseq);

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType("BYE")
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(channelIdentification)
                            .callId(callId.getCallId())
                            .sipMethod("BYE")
                            .build());
        } catch (Exception e) {
            log.error("发送BYE失败: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification, e);
            throw BizException.wrap("发送BYE失败: " + e.getMessage());
        }
    }

    /**
     * 发送回放暂停 INFO
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param callId     CallIdHeader
     */
    public void playbackPause(String deviceIdentification, String channelIdentification,
                              String deviceIp, int devicePort,
                              String transport, CallIdHeader callId,
                              String fromTag, String toTag, long cseq) {
        sendPlaybackInfo(deviceIdentification, channelIdentification, deviceIp, devicePort,
                transport, callId, sipMessageBuilder.buildMansrtspPause(), "PAUSE", fromTag, toTag, cseq);
    }

    /**
     * 发送回放恢复 INFO
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param callId     CallIdHeader
     */
    public void playbackResume(String deviceIdentification, String channelIdentification,
                               String deviceIp, int devicePort,
                               String transport, CallIdHeader callId,
                               String fromTag, String toTag, long cseq) {
        sendPlaybackInfo(deviceIdentification, channelIdentification, deviceIp, devicePort,
                transport, callId, sipMessageBuilder.buildMansrtspResume(), "RESUME", fromTag, toTag, cseq);
    }

    /**
     * 发送回放倍速控制 INFO
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param callId     CallIdHeader
     * @param speed      播放速度
     */
    public void playbackSpeed(String deviceIdentification, String channelIdentification,
                              String deviceIp, int devicePort,
                              String transport, CallIdHeader callId,
                              double speed,
                              String fromTag, String toTag, long cseq) {
        sendPlaybackInfo(deviceIdentification, channelIdentification, deviceIp, devicePort,
                transport, callId, sipMessageBuilder.buildMansrtspSpeed(speed), "SPEED:" + speed, fromTag, toTag, cseq);
    }

    /**
     * 发送回放拖拽 INFO
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param callId     CallIdHeader
     * @param seekTime   拖拽到的时间点（秒）
     */
    public void playbackSeek(String deviceIdentification, String channelIdentification,
                             String deviceIp, int devicePort,
                             String transport, CallIdHeader callId,
                             long seekTime,
                             String fromTag, String toTag, long cseq) {
        sendPlaybackInfo(deviceIdentification, channelIdentification, deviceIp, devicePort,
                transport, callId, sipMessageBuilder.buildMansrtspSeek(seekTime), "SEEK:" + seekTime, fromTag, toTag, cseq);
    }

    /**
     * 发送强制关键帧 INFO（GB/T 28181-2022）
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param callId     CallIdHeader
     */
    public void forceKeyFrame(String deviceIdentification, String channelIdentification,
                              String deviceIp, int devicePort,
                              String transport, CallIdHeader callId,
                              String fromTag, String toTag, long cseq) {
        sendPlaybackInfo(deviceIdentification, channelIdentification, deviceIp, devicePort,
                transport, callId, sipMessageBuilder.buildMansrtspForceKeyFrame(), "FORCE_KEY_FRAME", fromTag, toTag, cseq);
    }

    /**
     * 发送回放控制 INFO（通用方法，in-dialog）。
     *
     * @param fromTag 原 INVITE 的 From-tag
     * @param toTag   原 INVITE 2xx 响应的 To-tag
     * @param cseq    Dialog 内递增的 CSeq
     */
    private void sendPlaybackInfo(String deviceIdentification, String channelIdentification,
                                  String deviceIp, int devicePort,
                                  String transport, CallIdHeader callId,
                                  String mansrtspContent, String operation,
                                  String fromTag, String toTag, long cseq) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            Request request = sipMessageBuilder.buildInfoRequest(
                    deviceIdentification, channelIdentification, deviceIp, devicePort,
                    transport, callId, mansrtspContent, tenantConfig, fromTag, toTag, cseq);

            sipSender.transmitRequest(tenantConfig.getEffectiveHost(), request);

            log.info("发送回放控制INFO({}): deviceIdentification={}, channelIdentification={}, callId={}, fromTag={}, toTag={}, cseq={}",
                    operation, deviceIdentification, channelIdentification, callId.getCallId(), fromTag, toTag, cseq);

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType("PLAYBACK_CONTROL")
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(channelIdentification)
                            .callId(callId.getCallId())
                            .sipMethod("INFO")
                            .build());
        } catch (Exception e) {
            log.error("发送回放控制INFO({})失败: deviceIdentification={}, channelIdentification={}",
                    operation, deviceIdentification, channelIdentification, e);
            throw BizException.wrap("发送回放控制INFO失败: " + e.getMessage());
        }
    }
}
