package com.mqttsnet.thinglinks.video.gb28181.transmit.request;

import cn.hutool.core.util.StrUtil;
import com.google.common.primitives.Bytes;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description:处理接收IPCamera发来的SIP协议请求消息
 * @author: mqttsnet
 */
@Slf4j
public abstract class SIPRequestProcessorParent {

    @Autowired
    private SIPSender sipSender;

    public HeaderFactory getHeaderFactory() {
        try {
            return SipFactory.getInstance().createHeaderFactory();
        } catch (PeerUnavailableException e) {
            log.error("未处理的异常 ", e);
        }
        return null;
    }

    public MessageFactory getMessageFactory() {
        try {
            return SipFactory.getInstance().createMessageFactory();
        } catch (PeerUnavailableException e) {
            log.error("未处理的异常 ", e);
        }
        return null;
    }

    /***
     * 回复状态码
     * 100 trying
     * 200 OK
     * 400
     * 404
     */
    public SIPResponse responseAck(SIPRequest sipRequest, int statusCode) throws SipException, InvalidArgumentException, ParseException {
        return responseAck(sipRequest, statusCode, null);
    }

    public SIPResponse responseAck(SIPRequest sipRequest, int statusCode, String msg) throws SipException, InvalidArgumentException, ParseException {
        return responseAck(sipRequest, statusCode, msg, null);
    }

    public SIPResponse responseAck(SIPRequest sipRequest, int statusCode, String msg, ResponseAckExtraParam responseAckExtraParam) throws SipException, InvalidArgumentException, ParseException {
        if (sipRequest.getToHeader().getTag() == null) {
            sipRequest.getToHeader().setTag(SipUtils.getNewTag());
        }
        SIPResponse response = (SIPResponse) getMessageFactory().createResponse(statusCode, sipRequest);
        response.setStatusCode(statusCode);
        if (msg != null) {
            response.setReasonPhrase(msg);
        }

        if (responseAckExtraParam != null) {
            if (responseAckExtraParam.sipURI != null && sipRequest.getMethod().equals(Request.INVITE)) {
                log.debug("responseSdpAck SipURI: {}:{}", responseAckExtraParam.sipURI.getHost(), responseAckExtraParam.sipURI.getPort());
                Address concatAddress = SipFactory.getInstance().createAddressFactory().createAddress(
                        SipFactory.getInstance().createAddressFactory().createSipURI(responseAckExtraParam.sipURI.getUser(), responseAckExtraParam.sipURI.getHost() + ":" + responseAckExtraParam.sipURI.getPort()
                        ));
                response.addHeader(SipFactory.getInstance().createHeaderFactory().createContactHeader(concatAddress));
            }
            if (responseAckExtraParam.contentTypeHeader != null) {
                response.setContent(responseAckExtraParam.content, responseAckExtraParam.contentTypeHeader);
            }

            if (sipRequest.getMethod().equals(Request.SUBSCRIBE)) {
                if (responseAckExtraParam.expires == -1) {
                    log.error("[参数不全] 2xx的SUBSCRIBE回复，必须设置Expires header");
                } else {
                    ExpiresHeader expiresHeader = SipFactory.getInstance().createHeaderFactory().createExpiresHeader(responseAckExtraParam.expires);
                    response.addHeader(expiresHeader);
                }
            }
        } else {
            if (sipRequest.getMethod().equals(Request.SUBSCRIBE)) {
                log.error("[参数不全] 2xx的SUBSCRIBE回复，必须设置Expires header");
            }
        }

        // 发送response
        sipSender.transmitRequest(sipRequest.getLocalAddress().getHostAddress(), response);

        return response;
    }

    /**
     * 回复带sdp的200
     */
    public SIPResponse responseSdpAck(SIPRequest request, String sdp, VideoPlatformInfo videoPlatformInfo) throws SipException, InvalidArgumentException, ParseException {

        ContentTypeHeader contentTypeHeader = SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("APPLICATION", "SDP");

        // 兼容国标中的使用编码@域名作为RequestURI的情况
        SipURI sipURI = (SipURI) request.getRequestURI();
        if (sipURI.getPort() == -1) {
            sipURI = SipFactory.getInstance().createAddressFactory().createSipURI(videoPlatformInfo.getServerGBId(), videoPlatformInfo.getServerIp() + ":" + videoPlatformInfo.getServerPort());
        }
        ResponseAckExtraParam responseAckExtraParam = new ResponseAckExtraParam();
        responseAckExtraParam.contentTypeHeader = contentTypeHeader;
        responseAckExtraParam.content = sdp;
        responseAckExtraParam.sipURI = sipURI;

        SIPResponse sipResponse = responseAck(request, Response.OK, null, responseAckExtraParam);


        return sipResponse;
    }

    /**
     * 回复带xml的200
     */
    public SIPResponse responseXmlAck(SIPRequest request, String xml, VideoPlatformInfo videoPlatformInfo, Integer expires) throws SipException, InvalidArgumentException, ParseException {
        ContentTypeHeader contentTypeHeader = SipFactory.getInstance().createHeaderFactory().createContentTypeHeader("Application", "MANSCDP+xml");

        SipURI sipURI = (SipURI) request.getRequestURI();
        if (sipURI.getPort() == -1) {
            sipURI = SipFactory.getInstance().createAddressFactory().createSipURI(videoPlatformInfo.getServerGBId(), videoPlatformInfo.getServerIp() + ":" + videoPlatformInfo.getServerPort());
        }
        ResponseAckExtraParam responseAckExtraParam = new ResponseAckExtraParam();
        responseAckExtraParam.contentTypeHeader = contentTypeHeader;
        responseAckExtraParam.content = xml;
        responseAckExtraParam.sipURI = sipURI;
        responseAckExtraParam.expires = expires;
        return responseAck(request, Response.OK, null, responseAckExtraParam);
    }

    public Element getRootElement(RequestEvent evt) throws DocumentException {
        return getRootElement(evt, "gb2312");
    }

    public Element getRootElement(RequestEvent evt, String charset) throws DocumentException {

        byte[] rawContent = evt.getRequest().getRawContent();
        if (evt.getRequest().getContentLength().getContentLength() == 0
            || rawContent == null
            || rawContent.length == 0
            || ObjectUtils.isEmpty(new String(rawContent))) {
            return null;
        }

        if (charset == null) {
            charset = "gb2312";
        }
        SAXReader reader = new SAXReader();
        reader.setEncoding(charset);
        // 对海康出现的未转义字符做处理。
        String[] destStrArray = new String[]{"&lt;", "&gt;", "&amp;", "&apos;", "&quot;"};
        // 或许可扩展兼容其他字符
        char despChar = '&';
        byte destBye = (byte) despChar;
        List<Byte> result = new ArrayList<>();
        for (int i = 0; i < rawContent.length; i++) {
            if (rawContent[i] == destBye) {
                boolean resul = false;
                for (String destStr : destStrArray) {
                    if (i + destStr.length() <= rawContent.length) {
                        byte[] bytes = Arrays.copyOfRange(rawContent, i, i + destStr.length());
                        resul = resul || (Arrays.equals(bytes, destStr.getBytes()));
                    }
                }
                if (resul) {
                    result.add(rawContent[i]);
                }
            } else {
                result.add(rawContent[i]);
            }
        }
        byte[] bytesResult = Bytes.toArray(result);

        Document xml;
        try {
            xml = reader.read(new ByteArrayInputStream(bytesResult));
        } catch (DocumentException e) {
            log.warn("[xml解析异常]： 原文如下： \r\n{}", new String(bytesResult));
            log.warn("[xml解析异常]： 原文如下： 尝试兼容性处理");
            String[] xmlLineArray = new String(bytesResult).split("\\r?\\n");

            // 兼容海康的address字段带有<破换xml结构导致无法解析xml的问题
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : xmlLineArray) {
                if (s.startsWith("<Address")) {
                    continue;
                }
                stringBuilder.append(s);
            }
            xml = reader.read(new ByteArrayInputStream(stringBuilder.toString().getBytes()));
        }
        return xml.getRootElement();
    }

    /**
     * 处理租户ID
     * 并将租户ID放入上下文中
     *
     * @param sipRequest SIP请求
     */
    public void handlerTenantId(SIPRequest sipRequest) {
        SipUri sipUri = (SipUri) sipRequest.getRequestLine().getUri();
        String user = sipUri.getAuthority().getUser();
        if (StrUtil.isBlank(user)) {
            throw new BizException("The SIP server number cannot be empty");
        }
        // 设置租户ID(SIP服务器编码=租户ID)
        ContextUtil.setTenantId(user);
    }

    class ResponseAckExtraParam {
        String content;
        ContentTypeHeader contentTypeHeader;
        SipURI sipURI;
        int expires = -1;
    }


}
