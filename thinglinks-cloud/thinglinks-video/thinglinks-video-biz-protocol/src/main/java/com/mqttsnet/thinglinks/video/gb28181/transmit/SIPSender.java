package com.mqttsnet.thinglinks.video.gb28181.transmit;

import com.mqttsnet.thinglinks.video.config.gb28181.SipConfig;
import com.mqttsnet.thinglinks.video.gb28181.SipLayer;
import com.mqttsnet.thinglinks.video.gb28181.event.subscribe.SipSubscribe;
import gov.nist.javax.sip.SipProviderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.ClientTransaction;
import javax.sip.SipException;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 发送 SIP 消息。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SIPSender {

    private final SipLayer sipLayer;
    private final SipSubscribe sipSubscribe;
    private final SipConfig sipConfig;

    /**
     * 发送 SIP 消息（不等待响应）。
     */
    public void transmitRequest(String ip, Message message) throws SipException {
        ViaHeader viaHeader = (ViaHeader) message.getHeader(ViaHeader.NAME);
        String transport = resolveTransport(viaHeader);
        doSend(ip, transport, message);
    }

    /**
     * 异步发送 SIP 消息，返回 CompletableFuture 等待响应。
     *
     * @param ip      目标 IP
     * @param message SIP 消息
     * @param timeout 超时毫秒数（null 使用默认）
     * @return CompletableFuture，收到响应时 complete
     */
    public CompletableFuture<SipSubscribe.EventResult<?>> transmitRequestAsync(String ip, Message message, Long timeout) throws SipException {
        ViaHeader viaHeader = (ViaHeader) message.getHeader(ViaHeader.NAME);
        String transport = resolveTransport(viaHeader);

        CallIdHeader callIdHeader = (CallIdHeader) message.getHeader(CallIdHeader.NAME);
        CSeqHeader cSeqHeader = (CSeqHeader) message.getHeader(CSeqHeader.NAME);
        String key = callIdHeader.getCallId() + cSeqHeader.getSeqNumber();

        long timeoutMs = timeout != null ? timeout : sipConfig.getTimeout();
        CompletableFuture<SipSubscribe.EventResult<?>> future = sipSubscribe.subscribe(key, timeoutMs);

        try {
            doSend(ip, transport, message);
        } catch (SipException e) {
            sipSubscribe.cancel(key);
            throw e;
        }
        return future;
    }

    private String resolveTransport(ViaHeader viaHeader) {
        if (viaHeader == null) {
            log.warn("[消息头缺失]： ViaHeader， 使用默认的UDP方式处理数据");
            return "UDP";
        }
        return viaHeader.getTransport();
    }

    private void doSend(String ip, String transport, Message message) throws SipException {
        boolean isTcp = "TCP".equals(transport);
        SipProviderImpl primaryProvider = isTcp ? sipLayer.getTcpSipProvider(ip) : sipLayer.getUdpSipProvider(ip);
        if (primaryProvider == null) {
            log.error("[发送信息失败] 未找到{}://{}的监听信息", transport.toLowerCase(), ip);
            return;
        }

        // [诊断] 打印完整 SIP 报文（首尾各 100 行内控制），方便摄像头侧兼容性排查
        if (log.isInfoEnabled()) {
            String raw = message.toString();
            log.info("[SIP OUT] target={}, transport={}, length={}B, primaryProvider={}\n======== SIP 发送报文开始 ========\n{}\n======== SIP 发送报文结束 ========",
                    ip, transport, raw.length(),
                    primaryProvider.getListeningPoint(transport.toLowerCase()).getIPAddress(),
                    raw);
        }

        // 收集所有可用 Provider（主选优先）
        List<SipProviderImpl> providers = new ArrayList<>();
        providers.add(primaryProvider);
        Map<String, SipProviderImpl> allMap = isTcp
                ? sipLayer.getTcpSipProviderMap() : sipLayer.getUdpSipProviderMap();
        for (SipProviderImpl p : allMap.values()) {
            if (p != primaryProvider) {
                providers.add(p);
            }
        }

        // 尝试发送，主选失败时 fallback 到其他 Provider
        SipException lastException = null;
        for (SipProviderImpl provider : providers) {
            try {
                if (message instanceof Request) {
                    Request request = (Request) message;
                    // 关键：必须用 ClientTransaction 发送，JAIN-SIP 才会将响应
                    // （100 Trying / 200 OK / 4xx 等）通过 SipListener.processResponse 派发。
                    // 如果直接调 provider.sendRequest(Request)，是 stateless 发送，
                    // 响应到达时没有匹配的 ClientTransaction，JAIN-SIP 会静默丢弃，
                    // 表现就是"发 INVITE 后等不到响应"（历史 bug 根因）。
                    // ACK 例外：RFC 3261 要求 2xx 响应的 ACK 由 UAC 应用层无事务直接发送。
                    if (Request.ACK.equalsIgnoreCase(request.getMethod())) {
                        provider.sendRequest(request);
                    } else {
                        ClientTransaction clientTransaction = provider.getNewClientTransaction(request);
                        clientTransaction.sendRequest();
                    }
                } else if (message instanceof Response) {
                    provider.sendResponse((Response) message);
                }
                return; // 发送成功
            } catch (TransactionUnavailableException e) {
                lastException = new SipException("ClientTransaction 不可用: " + e.getMessage(), e);
                log.warn("[SIP发送] 通过 {} 创建事务失败，尝试下一个 Provider: {}",
                        provider.getListeningPoint(transport.toLowerCase()).getIPAddress(), e.getMessage());
            } catch (SipException e) {
                lastException = e;
                log.warn("[SIP发送] 通过 {} 发送失败，尝试下一个 Provider: {}",
                        provider.getListeningPoint(transport.toLowerCase()).getIPAddress(), e.getMessage());
            }
        }
        // 所有 Provider 都失败
        throw lastException;
    }

    public CallIdHeader getNewCallIdHeader(String ip, String transport) {
        if (ObjectUtils.isEmpty(transport)) {
            return sipLayer.getUdpSipProvider().getNewCallId();
        }
        SipProviderImpl sipProvider;
        if (ObjectUtils.isEmpty(ip)) {
            sipProvider = "TCP".equalsIgnoreCase(transport) ? sipLayer.getTcpSipProvider()
                    : sipLayer.getUdpSipProvider();
        } else {
            sipProvider = "TCP".equalsIgnoreCase(transport) ? sipLayer.getTcpSipProvider(ip)
                    : sipLayer.getUdpSipProvider(ip);
        }

        if (sipProvider == null) {
            sipProvider = sipLayer.getUdpSipProvider();
        }

        if (sipProvider != null) {
            return sipProvider.getNewCallId();
        } else {
            log.warn("[新建CallIdHeader失败]， ip={}, transport={}", ip, transport);
            return null;
        }
    }
}
