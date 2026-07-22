package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl;

import cn.hutool.core.util.IdUtil;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.SipCommandTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.transmit.observer.SIPProcessorObserver;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.ISIPRequestProcessor;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Response;
import java.text.ParseException;

/**
 * SIP INFO 请求处理器。
 * 处理录像回放控制通知（MANSRTSP），包括播放状态、进度等信息。
 *
 * GB/T 28181-2016 Section 9.7: INFO 请求用于在已建立的会话中传输控制信息。
 * Content-Type 为 Application/MANSRTSP 时表示回放控制。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InfoRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {

    public final String method = SipCommandTypeEnum.INFO.getValue();

    private static final String MANSRTSP_CONTENT_TYPE = "MANSRTSP";

    private final SIPProcessorObserver sipProcessorObserver;

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
        String deviceIdentification = SipUtils.getUserIdFromFromHeader(request);

        log.info("[收到INFO] 设备: {}, CallId={}", deviceIdentification, callId);

        try {
            ContentTypeHeader contentTypeHeader = (ContentTypeHeader) request.getHeader(ContentTypeHeader.NAME);
            if (contentTypeHeader != null && MANSRTSP_CONTENT_TYPE.equalsIgnoreCase(contentTypeHeader.getContentSubType())) {
                // 处理 MANSRTSP 回放控制消息
                String content = request.getRawContent() != null ? new String(request.getRawContent()) : "";
                log.info("[INFO-MANSRTSP] 设备: {}, CallId={}, 内容: {}", deviceIdentification, callId, content);
                handleMansrtspContent(deviceIdentification, callId, content);
            } else {
                log.info("[INFO] 设备: {}, CallId={}, ContentType: {}",
                        deviceIdentification, callId,
                        contentTypeHeader != null ? contentTypeHeader.getContentType() + "/" + contentTypeHeader.getContentSubType() : "null");
            }

            // 回复 200 OK
            responseAck(request, Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[INFO回复失败] 设备: {}, CallId={}, 错误: {}", deviceIdentification, callId, e.getMessage());
        }
    }

    /**
     * 处理 MANSRTSP 内容。
     * 解析回放控制消息，如 PLAY/PAUSE/TEARDOWN 等状态。
     *
     * @param deviceIdentification 设备编号
     * @param callId   Call-ID
     * @param content  MANSRTSP 消息体
     */
    private void handleMansrtspContent(String deviceIdentification, String callId, String content) {
        // 解析 MANSRTSP 内容，常见格式：
        // PLAY MANSRTSP/1.0
        // PAUSE MANSRTSP/1.0
        // TEARDOWN MANSRTSP/1.0
        if (content.contains("PLAY")) {
            log.info("[回放控制] 设备: {}, CallId={}, 操作: PLAY", deviceIdentification, callId);
        } else if (content.contains("PAUSE")) {
            log.info("[回放控制] 设备: {}, CallId={}, 操作: PAUSE", deviceIdentification, callId);
        } else if (content.contains("TEARDOWN")) {
            log.info("[回放控制] 设备: {}, CallId={}, 操作: TEARDOWN", deviceIdentification, callId);
        } else {
            log.info("[回放控制] 设备: {}, CallId={}, 未知操作内容", deviceIdentification, callId);
        }
    }
}
