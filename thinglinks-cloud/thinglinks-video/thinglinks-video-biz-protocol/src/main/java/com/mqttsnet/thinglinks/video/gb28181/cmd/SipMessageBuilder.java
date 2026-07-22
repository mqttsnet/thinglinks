package com.mqttsnet.thinglinks.video.gb28181.cmd;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.gb28181.SipLayer;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import gov.nist.javax.sip.SipProviderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * SIP 消息构建器。
 * 统一构建 GB/T 28181 所需的各类 SIP 请求消息（INVITE/BYE/INFO/MESSAGE/SUBSCRIBE），
 * 封装 javax.sip API 的复杂性，提供简洁的构建方法。
 * <p>
 * 所有 SIP 请求的 Header 构建遵循 GB/T 28181-2016/2022 标准。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SipMessageBuilder {

    private final SipLayer sipLayer;

    /**
     * 构建 INVITE 请求（实时点播/录像回放/文件下载/广播）
     *
     * @param deviceIdentification    目标设备国标编号
     * @param channelIdentification   通道国标编号
     * @param deviceIp    设备 IP
     * @param devicePort  设备 SIP 端口
     * @param sdpContent  SDP 内容
     * @param transport   传输协议（UDP/TCP）
     * @param ssrc        SSRC 值
     * @param callId      CallIdHeader（由 SIPSender.getNewCallIdHeader 生成）
     * @return INVITE 请求
     * @throws ParseException          解析异常
     * @throws InvalidArgumentException 参数异常
     * @throws PeerUnavailableException SIP 栈异常
     */
    public Request buildInviteRequest(String deviceIdentification, String channelIdentification,
                                      String deviceIp, int devicePort,
                                      String sdpContent, String transport,
                                      String ssrc, CallIdHeader callId,
                                      TenantSipConfig tenantConfig)
            throws ParseException, InvalidArgumentException, PeerUnavailableException {

        var addressFactory = SipFactory.getInstance().createAddressFactory();
        var headerFactory = SipFactory.getInstance().createHeaderFactory();
        var messageFactory = SipFactory.getInstance().createMessageFactory();

        // Request-URI: sip:channelIdentification@deviceIp:devicePort
        SipURI requestUri = addressFactory.createSipURI(channelIdentification, deviceIp + ":" + devicePort);

        // Via Header
        List<ViaHeader> viaHeaders = buildViaHeaders(headerFactory, transport, tenantConfig);

        // From Header: sip:sipId@sipDomain
        SipURI fromSipUri = addressFactory.createSipURI(tenantConfig.getSipId(), tenantConfig.getDomain());
        Address fromAddress = addressFactory.createAddress(fromSipUri);
        fromAddress.setDisplayName(tenantConfig.getSipId());
        FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, SipUtils.getNewFromTag());

        // To Header: sip:channelIdentification@deviceDomain
        SipURI toSipUri = addressFactory.createSipURI(channelIdentification, deviceIp + ":" + devicePort);
        Address toAddress = addressFactory.createAddress(toSipUri);
        toAddress.setDisplayName(channelIdentification);
        ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

        // CSeq Header
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.INVITE);

        // Max-Forwards
        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

        // 构建请求
        Request request = messageFactory.createRequest(requestUri, Request.INVITE,
                callId, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwardsHeader);

        // Contact Header
        SipURI contactUri = addressFactory.createSipURI(tenantConfig.getSipId(),
                tenantConfig.getEffectiveHost() + ":" + tenantConfig.getPort());
        Address contactAddress = addressFactory.createAddress(contactUri);
        request.addHeader(headerFactory.createContactHeader(contactAddress));

        // Subject Header: channelIdentification:ssrc,sipId:0（GB28181 规定的 Subject 格式）
        String subjectValue = channelIdentification + ":" + ssrc + "," + tenantConfig.getSipId() + ":0";
        SubjectHeader subjectHeader = headerFactory.createSubjectHeader(subjectValue);
        request.addHeader(subjectHeader);

        // Content-Type: APPLICATION/SDP
        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("APPLICATION", "SDP");
        request.setContent(sdpContent, contentTypeHeader);

        return request;
    }

    /**
     * 构建 BYE 请求（终止会话）
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param callId     CallIdHeader（使用会话中的 CallId）
     * @return BYE 请求
     * @throws ParseException          解析异常
     * @throws InvalidArgumentException 参数异常
     * @throws PeerUnavailableException SIP 栈异常
     */
    public Request buildByeRequest(String deviceIdentification, String channelIdentification,
                                   String deviceIp, int devicePort,
                                   String transport, CallIdHeader callId,
                                   TenantSipConfig tenantConfig,
                                   String fromTag, String toTag, long cseq)
            throws ParseException, InvalidArgumentException, PeerUnavailableException {

        var addressFactory = SipFactory.getInstance().createAddressFactory();
        var headerFactory = SipFactory.getInstance().createHeaderFactory();
        var messageFactory = SipFactory.getInstance().createMessageFactory();

        SipURI requestUri = addressFactory.createSipURI(channelIdentification, deviceIp + ":" + devicePort);

        List<ViaHeader> viaHeaders = buildViaHeaders(headerFactory, transport, tenantConfig);

        // in-dialog BYE：From-tag / To-tag / CSeq 必须由调用方从原 INVITE 事务取并传入，
        // builder 本身不做默认兜底（否则设备会以 481 拒绝，且隐藏真正的数据缺失问题）。
        SipURI fromSipUri = addressFactory.createSipURI(tenantConfig.getSipId(), tenantConfig.getDomain());
        Address fromAddress = addressFactory.createAddress(fromSipUri);
        FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, fromTag);

        SipURI toSipUri = addressFactory.createSipURI(channelIdentification, deviceIp + ":" + devicePort);
        Address toAddress = addressFactory.createAddress(toSipUri);
        ToHeader toHeader = headerFactory.createToHeader(toAddress, toTag);

        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, Request.BYE);
        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

        return messageFactory.createRequest(requestUri, Request.BYE,
                callId, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwardsHeader);
    }

    /**
     * 构建 INFO 请求（会话内信息传输，用于回放控制：暂停/恢复/倍速/拖拽/强制关键帧）
     *
     * @param deviceIdentification    设备国标编号
     * @param channelIdentification   通道国标编号
     * @param deviceIp    设备 IP
     * @param devicePort  设备 SIP 端口
     * @param transport   传输协议
     * @param callId      CallIdHeader
     * @param infoContent MANSRTSP 消息体内容
     * @return INFO 请求
     * @throws ParseException          解析异常
     * @throws InvalidArgumentException 参数异常
     * @throws PeerUnavailableException SIP 栈异常
     */
    public Request buildInfoRequest(String deviceIdentification, String channelIdentification,
                                    String deviceIp, int devicePort,
                                    String transport, CallIdHeader callId,
                                    String infoContent,
                                    TenantSipConfig tenantConfig,
                                    String fromTag, String toTag, long cseq)
            throws ParseException, InvalidArgumentException, PeerUnavailableException {

        var addressFactory = SipFactory.getInstance().createAddressFactory();
        var headerFactory = SipFactory.getInstance().createHeaderFactory();
        var messageFactory = SipFactory.getInstance().createMessageFactory();

        SipURI requestUri = addressFactory.createSipURI(channelIdentification, deviceIp + ":" + devicePort);

        List<ViaHeader> viaHeaders = buildViaHeaders(headerFactory, transport, tenantConfig);

        // in-dialog INFO：From-tag / To-tag / CSeq 由调用方从原 INVITE 事务传入，builder 不做兜底。
        SipURI fromSipUri = addressFactory.createSipURI(tenantConfig.getSipId(), tenantConfig.getDomain());
        Address fromAddress = addressFactory.createAddress(fromSipUri);
        FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, fromTag);

        SipURI toSipUri = addressFactory.createSipURI(channelIdentification, deviceIp + ":" + devicePort);
        Address toAddress = addressFactory.createAddress(toSipUri);
        ToHeader toHeader = headerFactory.createToHeader(toAddress, toTag);

        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, Request.INFO);
        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

        Request request = messageFactory.createRequest(requestUri, Request.INFO,
                callId, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwardsHeader);

        // Content-Type: Application/MANSRTSP
        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("Application", "MANSRTSP");
        request.setContent(infoContent, contentTypeHeader);

        return request;
    }

    /**
     * 构建 MESSAGE 请求（设备控制/查询：PTZ、布防、设备信息查询等）
     *
     * @param deviceIdentification   目标设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param xmlContent MANSCDP+xml 消息体内容
     * @param callId     CallIdHeader
     * @return MESSAGE 请求
     * @throws ParseException          解析异常
     * @throws InvalidArgumentException 参数异常
     * @throws PeerUnavailableException SIP 栈异常
     */
    public Request buildMessageRequest(String deviceIdentification,
                                       String deviceIp, int devicePort,
                                       String transport, String xmlContent,
                                       CallIdHeader callId,
                                       TenantSipConfig tenantConfig)
            throws ParseException, InvalidArgumentException, PeerUnavailableException {

        var addressFactory = SipFactory.getInstance().createAddressFactory();
        var headerFactory = SipFactory.getInstance().createHeaderFactory();
        var messageFactory = SipFactory.getInstance().createMessageFactory();

        // Request-URI: sip:deviceIdentification@deviceIp:devicePort
        SipURI requestUri = addressFactory.createSipURI(deviceIdentification, deviceIp + ":" + devicePort);

        List<ViaHeader> viaHeaders = buildViaHeaders(headerFactory, transport, tenantConfig);

        SipURI fromSipUri = addressFactory.createSipURI(tenantConfig.getSipId(), tenantConfig.getDomain());
        Address fromAddress = addressFactory.createAddress(fromSipUri);
        FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, SipUtils.getNewFromTag());

        SipURI toSipUri = addressFactory.createSipURI(deviceIdentification, deviceIp + ":" + devicePort);
        Address toAddress = addressFactory.createAddress(toSipUri);
        ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.MESSAGE);
        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

        Request request = messageFactory.createRequest(requestUri, Request.MESSAGE,
                callId, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwardsHeader);

        // Content-Type: Application/MANSCDP+xml
        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("Application", "MANSCDP+xml");
        request.setContent(xmlContent, contentTypeHeader);

        return request;
    }

    /**
     * 构建 SUBSCRIBE 请求（订阅事件：目录变更/移动位置/告警）
     *
     * @param deviceIdentification   目标设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param xmlContent MANSCDP+xml 消息体内容
     * @param callId     CallIdHeader
     * @param expires    订阅有效期（秒，0 表示取消订阅）
     * @param eventType  事件类型（如 "Catalog"、"Alarm"、"MobilePosition"）
     * @return SUBSCRIBE 请求
     * @throws ParseException          解析异常
     * @throws InvalidArgumentException 参数异常
     * @throws PeerUnavailableException SIP 栈异常
     */
    public Request buildSubscribeRequest(String deviceIdentification,
                                         String deviceIp, int devicePort,
                                         String transport, String xmlContent,
                                         CallIdHeader callId,
                                         int expires, String eventType,
                                         TenantSipConfig tenantConfig)
            throws ParseException, InvalidArgumentException, PeerUnavailableException {

        var addressFactory = SipFactory.getInstance().createAddressFactory();
        var headerFactory = SipFactory.getInstance().createHeaderFactory();
        var messageFactory = SipFactory.getInstance().createMessageFactory();

        SipURI requestUri = addressFactory.createSipURI(deviceIdentification, deviceIp + ":" + devicePort);

        List<ViaHeader> viaHeaders = buildViaHeaders(headerFactory, transport, tenantConfig);

        SipURI fromSipUri = addressFactory.createSipURI(tenantConfig.getSipId(), tenantConfig.getDomain());
        Address fromAddress = addressFactory.createAddress(fromSipUri);
        FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, SipUtils.getNewFromTag());

        SipURI toSipUri = addressFactory.createSipURI(deviceIdentification, deviceIp + ":" + devicePort);
        Address toAddress = addressFactory.createAddress(toSipUri);
        ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.SUBSCRIBE);
        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

        Request request = messageFactory.createRequest(requestUri, Request.SUBSCRIBE,
                callId, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwardsHeader);

        // Contact Header
        SipURI contactUri = addressFactory.createSipURI(tenantConfig.getSipId(),
                tenantConfig.getEffectiveHost() + ":" + tenantConfig.getPort());
        Address contactAddress = addressFactory.createAddress(contactUri);
        request.addHeader(headerFactory.createContactHeader(contactAddress));

        // Expires Header
        request.addHeader(headerFactory.createExpiresHeader(expires));

        // Event Header（GB28181 订阅事件类型）
        EventHeader eventHeader = headerFactory.createEventHeader(eventType);
        request.addHeader(eventHeader);

        // Content-Type: Application/MANSCDP+xml
        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("Application", "MANSCDP+xml");
        request.setContent(xmlContent, contentTypeHeader);

        return request;
    }

    /**
     * 构建 MANSRTSP 暂停消息体
     *
     * @return MANSRTSP PAUSE 内容
     */
    public String buildMansrtspPause() {
        return "PAUSE MANSRTSP/1.0\r\nCSeq: 1\r\nPauseTime: now\r\n";
    }

    /**
     * 构建 MANSRTSP 恢复播放消息体
     *
     * @return MANSRTSP PLAY 内容
     */
    public String buildMansrtspResume() {
        return "PLAY MANSRTSP/1.0\r\nCSeq: 2\r\nRange: npt=now-\r\n";
    }

    /**
     * 构建 MANSRTSP 倍速播放消息体
     *
     * @param speed 播放速度（0.25/0.5/1/2/4 等）
     * @return MANSRTSP PLAY 内容
     */
    public String buildMansrtspSpeed(double speed) {
        return "PLAY MANSRTSP/1.0\r\nCSeq: 3\r\nScale: " + speed + "\r\n";
    }

    /**
     * 构建 MANSRTSP 拖拽播放消息体
     *
     * @param seekTime 拖拽到的时间点（NTP 秒数或相对位置）
     * @return MANSRTSP PLAY 内容
     */
    public String buildMansrtspSeek(long seekTime) {
        return "PLAY MANSRTSP/1.0\r\nCSeq: 4\r\nRange: npt=" + seekTime + "-\r\n";
    }

    /**
     * 构建 MANSRTSP 强制关键帧消息体（GB/T 28181-2022）
     *
     * @return MANSRTSP 消息内容
     */
    public String buildMansrtspForceKeyFrame() {
        return "PLAY MANSRTSP/1.0\r\nCSeq: 5\r\nForce-I-Frame: true\r\n";
    }

    /**
     * 构建 Via Header 列表
     *
     * @param headerFactory Header 工厂
     * @param transport     传输协议（UDP/TCP）
     * @return Via Header 列表
     * @throws ParseException          解析异常
     * @throws InvalidArgumentException 参数异常
     */
    /**
     * 构建 REGISTER 请求（作为 UAC 向上级平台注册）
     *
     * @param serverGbId    上级平台国标编号
     * @param serverIp      上级平台 SIP IP
     * @param serverPort    上级平台 SIP 端口
     * @param deviceGbId    我方设备国标编号
     * @param deviceDomain  我方 SIP 域
     * @param localIp       本地 SIP IP
     * @param localPort     本地 SIP 端口
     * @param transport     传输协议
     * @param expires       注册有效期（秒），0 = 注销
     * @param callId        CallIdHeader
     * @return REGISTER 请求
     */
    public Request buildRegisterRequest(String serverGbId, String serverIp, int serverPort,
                                        String deviceGbId, String deviceDomain,
                                        String localIp, int localPort,
                                        String transport, int expires,
                                        CallIdHeader callId)
            throws ParseException, InvalidArgumentException, PeerUnavailableException {

        var addressFactory = SipFactory.getInstance().createAddressFactory();
        var headerFactory = SipFactory.getInstance().createHeaderFactory();
        var messageFactory = SipFactory.getInstance().createMessageFactory();

        // Request-URI: sip:serverGbId@serverIp:serverPort
        SipURI requestUri = addressFactory.createSipURI(serverGbId, serverIp + ":" + serverPort);

        // Via Header (使用本地地址)
        List<ViaHeader> viaHeaders = new ArrayList<>(1);
        ViaHeader viaHeader = headerFactory.createViaHeader(
                localIp, localPort,
                StrUtil.isBlank(transport) ? "UDP" : transport,
                SipUtils.getNewViaTag());
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);

        // From: sip:deviceGbId@deviceDomain
        SipURI fromSipUri = addressFactory.createSipURI(deviceGbId, deviceDomain);
        Address fromAddress = addressFactory.createAddress(fromSipUri);
        fromAddress.setDisplayName(deviceGbId);
        FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, SipUtils.getNewFromTag());

        // To: sip:serverGbId@serverDomain
        SipURI toSipUri = addressFactory.createSipURI(serverGbId, serverIp + ":" + serverPort);
        Address toAddress = addressFactory.createAddress(toSipUri);
        toAddress.setDisplayName(serverGbId);
        ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

        // CSeq
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.REGISTER);

        // Max-Forwards
        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

        // Build request
        Request request = messageFactory.createRequest(requestUri, Request.REGISTER,
                callId, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwardsHeader);

        // Expires
        ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(expires);
        request.addHeader(expiresHeader);

        // Contact: sip:deviceGbId@localIp:localPort
        SipURI contactUri = addressFactory.createSipURI(deviceGbId, localIp + ":" + localPort);
        Address contactAddress = addressFactory.createAddress(contactUri);
        request.addHeader(headerFactory.createContactHeader(contactAddress));

        return request;
    }

    /**
     * 为 REGISTER 请求添加摘要认证头
     *
     * @param request     原始 REGISTER 请求
     * @param username    认证用户名
     * @param password    认证密码
     * @param realm       认证域（从 401 响应的 WWWAuthenticateHeader 获取）
     * @param nonce       随机数（从 401 响应的 WWWAuthenticateHeader 获取）
     * @param requestUri  请求 URI 字符串
     */
    public void addAuthorizationHeader(Request request, String username, String password,
                                       String realm, String nonce, String requestUri)
            throws ParseException, PeerUnavailableException {
        var headerFactory = SipFactory.getInstance().createHeaderFactory();

        // 计算摘要: response = MD5(MD5(A1):nonce:MD5(A2))
        // A1 = username:realm:password, A2 = REGISTER:requestUri
        String a1 = username + ":" + realm + ":" + password;
        String a2 = Request.REGISTER + ":" + requestUri;
        String ha1 = cn.hutool.crypto.SecureUtil.md5(a1);
        String ha2 = cn.hutool.crypto.SecureUtil.md5(a2);
        String response = cn.hutool.crypto.SecureUtil.md5(ha1 + ":" + nonce + ":" + ha2);

        AuthorizationHeader authHeader = headerFactory.createAuthorizationHeader("Digest");
        authHeader.setUsername(username);
        authHeader.setRealm(realm);
        authHeader.setNonce(nonce);
        authHeader.setURI(SipFactory.getInstance().createAddressFactory().createURI(requestUri));
        authHeader.setResponse(response);
        authHeader.setAlgorithm("MD5");

        request.addHeader(authHeader);
    }

    private List<ViaHeader> buildViaHeaders(HeaderFactory headerFactory, String transport, TenantSipConfig tenantConfig)
            throws ParseException, InvalidArgumentException {
        List<ViaHeader> viaHeaders = new ArrayList<>(1);
        ViaHeader viaHeader = headerFactory.createViaHeader(
                tenantConfig.getEffectiveHost(), tenantConfig.getPort(),
                StrUtil.isBlank(transport) ? "UDP" : transport,
                SipUtils.getNewViaTag());
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);
        return viaHeaders;
    }
}
