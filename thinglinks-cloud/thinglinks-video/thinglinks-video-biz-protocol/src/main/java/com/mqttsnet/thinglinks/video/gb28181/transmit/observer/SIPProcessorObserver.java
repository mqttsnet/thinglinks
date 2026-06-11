package com.mqttsnet.thinglinks.video.gb28181.transmit.observer;

import com.mqttsnet.thinglinks.video.gb28181.event.subscribe.SipSubscribe;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.ISIPRequestProcessor;
import com.mqttsnet.thinglinks.video.gb28181.transmit.response.ISIPResponseProcessor;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import gov.nist.javax.sip.message.SIPRequest;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.Transaction;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: SIP信令处理类观察者
 * @author: mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SIPProcessorObserver implements ISIPProcessorObserver {

    private static final Map<String, ISIPRequestProcessor> requestProcessorMap = new ConcurrentHashMap<>();
    private static final Map<String, ISIPResponseProcessor> responseProcessorMap = new ConcurrentHashMap<>();

    private final SipSubscribe sipSubscribe;

    public void addRequestProcessor(String method, ISIPRequestProcessor processor) {
        requestProcessorMap.put(method, processor);
    }

    public void addResponseProcessor(String method, ISIPResponseProcessor processor) {
        responseProcessorMap.put(method, processor);
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        String method = requestEvent.getRequest().getMethod();
        ISIPRequestProcessor sipRequestProcessor = requestProcessorMap.get(method);
        if (sipRequestProcessor == null) {
            log.warn("不支持方法{}的request", method);
            return;
        }
        requestProcessorMap.get(method).process(requestEvent);
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        SIPResponse response = (SIPResponse) responseEvent.getResponse();
        int status = response.getStatusCode();

        // [诊断] 任何 SIP 响应都记录一条 INFO，方便排查"设备有没有回响应"
        if (log.isInfoEnabled()) {
            CSeqHeader cSeq = response.getCSeqHeader();
            CallIdHeader cid = response.getCallIdHeader();
            log.info("[SIP IN ] 响应: status={} {}, method={}, cseq={}, callId={}",
                    status, response.getReasonPhrase(),
                    cSeq != null ? cSeq.getMethod() : "?",
                    cSeq != null ? cSeq.getSeqNumber() : "?",
                    cid != null ? cid.getCallId() : "?");
        }

        // Success
        if (((status >= Response.OK) && (status < Response.MULTIPLE_CHOICES)) || status == Response.UNAUTHORIZED) {
            CallIdHeader callIdHeader = response.getCallIdHeader();
            CSeqHeader cSeqHeader = response.getCSeqHeader();

            // === 关键：INVITE 的 2xx 必须回 ACK，否则摄像头会无限重发 200 OK 占满日志 ===
            // RFC 3261 §13.2.2.4：INVITE 2xx 的 ACK 由应用层直接发送（不走 ClientTransaction），
            // 只有带 Dialog 的响应才能 createAck。
            if (status >= Response.OK && status < Response.MULTIPLE_CHOICES
                    && cSeqHeader != null && Request.INVITE.equals(cSeqHeader.getMethod())) {
                Dialog dialog = responseEvent.getDialog();
                if (dialog != null) {
                    try {
                        Request ack = dialog.createAck(cSeqHeader.getSeqNumber());
                        dialog.sendAck(ack);
                        log.info("[SIP ACK] 已对 INVITE 2xx 响应发送 ACK: callId={}, cseq={}",
                                callIdHeader != null ? callIdHeader.getCallId() : "?",
                                cSeqHeader.getSeqNumber());
                    } catch (Exception e) {
                        log.error("[SIP ACK] 发送 INVITE 2xx 的 ACK 失败: callId={}, error={}",
                                callIdHeader != null ? callIdHeader.getCallId() : "?", e.getMessage(), e);
                    }
                } else {
                    log.warn("[SIP ACK] 收到 INVITE 2xx 但 Dialog 为空（可能未走 ClientTransaction 发起），无法自动 ACK: callId={}",
                            callIdHeader != null ? callIdHeader.getCallId() : "?");
                }
            }

            if (callIdHeader != null) {
                String key = callIdHeader.getCallId() + cSeqHeader.getSeqNumber();
                boolean subscribed = sipSubscribe.hasSubscribe(key);
                log.info("[SIP 订阅派发] status={}, key={}, subscribed={}, mapSize={}",
                        status, key, subscribed, sipSubscribe.size());
                if (subscribed) {
                    try {
                        SipSubscribe.EventResult<ResponseEvent> eventResult = new SipSubscribe.EventResult<>(responseEvent);
                        sipSubscribe.complete(key, eventResult);
                    } catch (Throwable t) {
                        log.error("[SIP 订阅派发] 构造 EventResult 或 complete 失败: key={}", key, t);
                    }
                }
            }
            ISIPResponseProcessor sipRequestProcessor = responseProcessorMap.get(response.getCSeqHeader().getMethod());
            if (sipRequestProcessor != null) {
                sipRequestProcessor.process(responseEvent);
            }
        } else if ((status >= Response.TRYING) && (status < Response.OK)) {
            // 1xx provisional, ignore（日志已在上面 INFO 打出来）
        } else {
            log.warn("接收到失败的response响应！status：" + status + ",message:" + response.getReasonPhrase());
            if (responseEvent.getResponse() != null && !sipSubscribe.isEmpty()) {
                CallIdHeader callIdHeader = response.getCallIdHeader();
                CSeqHeader cSeqHeader = response.getCSeqHeader();
                if (callIdHeader != null) {
                    String key = callIdHeader.getCallId() + cSeqHeader.getSeqNumber();
                    if (sipSubscribe.hasSubscribe(key)) {
                        SipSubscribe.EventResult<ResponseEvent> eventResult = new SipSubscribe.EventResult<>(responseEvent);
                        eventResult.type = SipSubscribe.EventResultType.failedResult;
                        sipSubscribe.complete(key, eventResult);
                    }
                }
            }
            if (responseEvent.getDialog() != null) {
                responseEvent.getDialog().delete();
            }
        }
    }

    /**
     * SIP 层检测到事务超时（Timer F/H 过期）。必须信号对应 Future，否则业务线程会阻塞到壁钟超时。
     */
    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        Transaction transaction = timeoutEvent.isServerTransaction()
                ? timeoutEvent.getServerTransaction() : timeoutEvent.getClientTransaction();
        SIPRequest request = (SIPRequest) transaction.getRequest();
        String callId = request.getCallIdHeader().getCallId();
        long cseq = request.getCSeqHeader().getSeqNumber();
        log.warn("[SIP 超时] callId={}, method={}, cseq={}", callId, request.getCSeqHeader().getMethod(), cseq);
        String key = callId + cseq;
        if (sipSubscribe.hasSubscribe(key)) {
            sipSubscribe.complete(key, new SipSubscribe.EventResult<>(timeoutEvent));
        }
    }

    /**
     * SIP 层检测到网络 IO 异常（发送失败 / 连接断开）。
     * 事件不带 transaction 引用，无法精准匹配 callId；记录告警，等后续 TimeoutEvent 信号 future。
     */
    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        log.warn("[SIP IO异常] host={}, port={}, transport={}",
                exceptionEvent.getHost(), exceptionEvent.getPort(), exceptionEvent.getTransport());
    }

    /**
     * 事务终止。如果对应 Future 还活着，完成它避免悬挂（异常路径：没收到响应就被终止）。
     */
    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent event) {
        Transaction transaction = event.isServerTransaction()
                ? event.getServerTransaction() : event.getClientTransaction();
        SIPRequest request = (SIPRequest) transaction.getRequest();
        String key = request.getCallIdHeader().getCallId() + request.getCSeqHeader().getSeqNumber();
        if (sipSubscribe.hasSubscribe(key)) {
            log.warn("[SIP 事务终止] Future 仍活跃，complete 避免阻塞: key={}", key);
            sipSubscribe.complete(key, new SipSubscribe.EventResult<>(event));
        }
    }

    /**
     * Dialog 终止事件（BYE / 超时 / 异常）。应用层的 SsrcTransaction 清理由 stop/bye 流程负责。
     */
    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        log.debug("[SIP Dialog 终止] callId={}", dialogTerminatedEvent.getDialog().getCallId().getCallId());
    }
}
