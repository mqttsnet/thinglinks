package com.mqttsnet.thinglinks.video.gb28181.cmd;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.enumeration.stream.StreamModeEnum;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.SipCommandEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SipCommandSentEventSource;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 * 语音广播/对讲命令发送器。
 * 负责构建和发送 GB/T 28181 语音广播和语音对讲相关的 SIP 命令。
 * <p>
 * 广播流程：
 * 1. 发送 Broadcast 通知（SIP MESSAGE）告知设备准备接收语音
 * 2. 设备回复 200 OK 后，发送 INVITE 建立语音流通道
 * 3. 语音流建立后开始广播/对讲
 * 4. 发送 BYE 终止广播/对讲
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastCommander {

    private final SipMessageBuilder sipMessageBuilder;
    private final SdpBuilder sdpBuilder;
    private final SIPSender sipSender;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final SipCommandEventPublisher sipCommandEventPublisher;

    /**
     * SN 序列号生成器
     */
    private final AtomicInteger snGenerator = new AtomicInteger(1);

    /**
     * 发送广播通知（第一步：通知设备准备接收广播）
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     */
    public void broadcastNotify(String deviceIdentification, String channelIdentification,
                                String deviceIp, int devicePort,
                                String transport) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            String sn = String.valueOf(snGenerator.getAndIncrement());
            String xml = buildBroadcastNotifyXml(sn, tenantConfig.getSipId(), channelIdentification);

            CallIdHeader callId = sipSender.getNewCallIdHeader(tenantConfig.getEffectiveHost(), transport);
            Request request = sipMessageBuilder.buildMessageRequest(
                    deviceIdentification, deviceIp, devicePort, transport, xml, callId, tenantConfig);

            sipSender.transmitRequest(tenantConfig.getEffectiveHost(), request);

            log.info("发送广播通知: deviceIdentification={}, channelIdentification={}, callId={}",
                    deviceIdentification, channelIdentification, callId.getCallId());

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType("BROADCAST_NOTIFY")
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(channelIdentification)
                            .callId(callId.getCallId())
                            .sipMethod("MESSAGE")
                            .build());
        } catch (Exception e) {
            log.error("发送广播通知失败: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification, e);
            throw BizException.wrap("发送广播通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送广播/对讲 INVITE（第二步：建立音频流通道）
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param streamMode 传输模式
     * @param mediaIp    流媒体服务器 IP
     * @param rtpPort    RTP 端口
     * @param ssrc       SSRC 值
     * @param callId     CallIdHeader
     * @return SIP INVITE 请求
     */
    public Request broadcastInvite(String deviceIdentification, String channelIdentification,
                                   String deviceIp, int devicePort,
                                   String transport, StreamModeEnum streamMode,
                                   String mediaIp, int rtpPort, String ssrc,
                                   CallIdHeader callId) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            String sdp = sdpBuilder.buildBroadcastSdp(mediaIp, rtpPort, ssrc,
                    streamMode, deviceIdentification, channelIdentification);

            Request request = sipMessageBuilder.buildInviteRequest(
                    deviceIdentification, channelIdentification, deviceIp, devicePort,
                    sdp, transport, ssrc, callId, tenantConfig);

            sipSender.transmitRequest(tenantConfig.getEffectiveHost(), request);

            log.info("发送广播INVITE: deviceIdentification={}, channelIdentification={}, ssrc={}, callId={}",
                    deviceIdentification, channelIdentification, ssrc, callId.getCallId());

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType("BROADCAST")
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(channelIdentification)
                            .callId(callId.getCallId())
                            .sipMethod("INVITE")
                            .build());

            return request;
        } catch (Exception e) {
            log.error("发送广播INVITE失败: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification, e);
            throw BizException.wrap("发送广播INVITE失败: " + e.getMessage());
        }
    }

    /**
     * 停止广播/对讲（发送 BYE）
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param callId     CallIdHeader
     */
    /**
     * 停止广播/对讲 BYE（in-dialog）。
     *
     * @param fromTag 原 INVITE 的 From-tag
     * @param toTag   INVITE 2xx 响应的 To-tag
     * @param cseq    递增的 CSeq（SsrcTransaction.cseq + 1）
     */
    public void broadcastBye(String deviceIdentification, String channelIdentification,
                             String deviceIp, int devicePort,
                             String transport, CallIdHeader callId,
                             String fromTag, String toTag, long cseq) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            Request request = sipMessageBuilder.buildByeRequest(
                deviceIdentification, channelIdentification, deviceIp, devicePort,
                transport, callId, tenantConfig, fromTag, toTag, cseq);

            sipSender.transmitRequest(tenantConfig.getEffectiveHost(), request);

            log.info("发送广播BYE: deviceIdentification={}, channelIdentification={}, callId={}, fromTag={}, toTag={}, cseq={}",
                deviceIdentification, channelIdentification, callId.getCallId(), fromTag, toTag, cseq);

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType("BROADCAST_BYE")
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(channelIdentification)
                            .callId(callId.getCallId())
                            .sipMethod("BYE")
                            .build());
        } catch (Exception e) {
            log.error("发送广播BYE失败: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification, e);
            throw BizException.wrap("发送广播BYE失败: " + e.getMessage());
        }
    }

    /**
     * 构建广播通知 XML
     *
     * @param sn        序列号
     * @param sourceId  广播源编号（本平台 SIP 编号）
     * @param targetId  目标通道编号
     * @return 广播通知 XML 字符串
     */
    private String buildBroadcastNotifyXml(String sn, String sourceId, String targetId) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Notify>
                <CmdType>Broadcast</CmdType>
                <SN>%s</SN>
                <SourceID>%s</SourceID>
                <TargetID>%s</TargetID>
                </Notify>""".formatted(sn, sourceId, targetId);
    }
}
