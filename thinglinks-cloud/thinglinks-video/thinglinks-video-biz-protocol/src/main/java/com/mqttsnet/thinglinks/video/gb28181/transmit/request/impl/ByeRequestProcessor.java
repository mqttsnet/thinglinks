package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.SsrcTransaction;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.SipCommandTypeEnum;
import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.session.InviteSessionService;
import com.mqttsnet.thinglinks.video.gb28181.transmit.observer.SIPProcessorObserver;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.ISIPRequestProcessor;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.service.device.VideoChannelService;
import com.mqttsnet.thinglinks.video.service.stream.PlayService;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;

/**
 * SIP BYE 请求处理器。
 * 处理设备或平台发送的 BYE 请求，终止 SIP 会话并释放相关资源（SSRC、RTP 端口）。
 *
 * GB/T 28181-2016 Section 9.4: BYE 请求用于终止已建立的媒体会话。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class ByeRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {

    public final String method = SipCommandTypeEnum.BYE.getValue();

    @Autowired
    private SIPProcessorObserver sipProcessorObserver;

    @Autowired
    private InviteSessionService inviteSessionService;

    @Autowired
    private VideoChannelService videoChannelService;

    @Autowired
    private PlayService playService;

    @Override
    public void afterPropertiesSet() {
        sipProcessorObserver.addRequestProcessor(method, this);
    }

    @Override
    public void process(RequestEvent evt) {
        MDC.put(ContextConstants.TRACE_ID_HEADER, IdUtil.fastSimpleUUID());
        SIPRequest request = (SIPRequest) evt.getRequest();
        handlerTenantId(request);

        String callId = request.getCallId().getCallId();
        String fromUser = SipUtils.getUserIdFromFromHeader(request);

        log.info("[收到BYE] from={}, CallId={}", fromUser, callId);

        // BYE 在实时点播场景里 From URI 用户名是**通道国标编号**，但 SsrcTransaction 按 deviceIdentification 索引，
        // 所以这里先把 fromUser 当 deviceId 直查一次，没命中再按 channelId 反查所属设备。
        String resolvedDeviceId = null;
        String resolvedChannelId = null;
        InviteSessionTypeEnum sessionType = null;

        if (StrUtil.isNotBlank(fromUser)) {
            // 1. fromUser 直接当 deviceId
            SsrcTransaction tx = inviteSessionService.getTransaction(fromUser, callId).orElse(null);
            if (tx != null) {
                resolvedDeviceId = fromUser;
                resolvedChannelId = tx.getChannelIdentification();
                sessionType = tx.getType();
            } else {
                // 2. 否则反查 channel → device
                try {
                    VideoChannelResultVO channel = videoChannelService.getByChannelIdentification(fromUser);
                    if (channel != null && StrUtil.isNotBlank(channel.getDeviceIdentification())) {
                        resolvedDeviceId = channel.getDeviceIdentification();
                        resolvedChannelId = fromUser;
                        SsrcTransaction txByDevice = inviteSessionService.getTransaction(resolvedDeviceId, callId).orElse(null);
                        if (txByDevice != null) {
                            sessionType = txByDevice.getType();
                        }
                    }
                } catch (Exception e) {
                    log.warn("[BYE] 通过 channel 反查设备失败，fromUser={}, error={}", fromUser, e.getMessage());
                }
            }
        }

        try {
            // 实时点播会话：交给 PlayService 走完整清理（关 RTP server / 释放 SSRC / 清 StreamInfo / 发事件），
            // 不走那条只 closeSession 的路径，避免 ZLM 上的 RTP 接收服务器残留导致下次 openRtpServer 撞 "stream already exists"。
            if (sessionType == InviteSessionTypeEnum.PLAY
                    && StrUtil.isNotBlank(resolvedDeviceId) && StrUtil.isNotBlank(resolvedChannelId)) {
                playService.handleRemoteBye(resolvedDeviceId, resolvedChannelId, callId);
            } else if (StrUtil.isNotBlank(resolvedDeviceId)) {
                // 其他类型（broadcast / 老数据等）退回到旧逻辑：仅释放会话级资源
                inviteSessionService.closeSession(resolvedDeviceId, callId, "BYE from device");
            } else {
                log.warn("[BYE] 未找到对应会话，仅回复 200 OK 不做清理: fromUser={}, callId={}", fromUser, callId);
            }

            // 回复 200 OK：必须用 ServerTransaction，否则 JAIN-SIP 会抛 "Transaction exists -- cannot send response statelessly"
            responseAck(request, Response.OK);
            log.info("[BYE处理完成] device={}, channel={}, fromUser={}, CallId={}",
                    resolvedDeviceId, resolvedChannelId, fromUser, callId);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[BYE回复失败] device={}, channel={}, fromUser={}, CallId={}, 错误: {}",
                    resolvedDeviceId, resolvedChannelId, fromUser, callId, e.getMessage());
        }
    }
}
