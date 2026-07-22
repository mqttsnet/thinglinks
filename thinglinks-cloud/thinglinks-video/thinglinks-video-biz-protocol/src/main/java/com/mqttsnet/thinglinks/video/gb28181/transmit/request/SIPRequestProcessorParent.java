package com.mqttsnet.thinglinks.video.gb28181.transmit.request;

import cn.hutool.core.util.StrUtil;
import com.google.common.primitives.Bytes;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigCacheVO;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigHolder;
import com.mqttsnet.thinglinks.video.config.gb28181.SipConfig;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.gb28181.SipLayer;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.SIPTransaction;
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
import javax.sip.ServerTransaction;
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

    @Autowired
    private TenantSipConfigProvider tenantSipConfigProvider;

    @Autowired
    private SipLayer sipLayer;

    @Autowired
    private SipConfig sipConfig;

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

        // 发送 response：必须按 JAIN-SIP 的事务状态来 ——
        //   - 如果 SIPRequest 已经被绑定 ServerTransaction（in-dialog 请求如 BYE / re-INVITE / SUBSCRIBE 等
        //     都会自动建 ServerTransaction），就走 tx.sendResponse；
        //   - 如果是无事务的 stateless 请求（很多 REGISTER / 首次 MESSAGE 实现里），才回退 provider.sendResponse。
        //
        // 早先固定走 sipSender.transmitRequest → provider.sendResponse 的 stateless 路径：
        // 对 BYE 这种已自动建 ServerTransaction 的请求，JAIN-SIP 会抛
        // "Transaction exists -- cannot send response statelessly"，结果设备一直收不到 200 OK，
        // 反复重发 BYE，本端事务/dialog 也一直处于半关状态。
        SIPTransaction sipTransaction = (SIPTransaction) sipRequest.getTransaction();
        if (sipTransaction instanceof ServerTransaction) {
            try {
                ((ServerTransaction) sipTransaction).sendResponse(response);
            } catch (SipException e) {
                // 走到这里通常是事务已经因为超时被 JAIN-SIP terminate 掉，再退回 stateless 兜底
                log.warn("[SIP响应] ServerTransaction 发送失败，回退 stateless: {}", e.getMessage());
                sipSender.transmitRequest(sipRequest.getLocalAddress().getHostAddress(), response);
            }
        } else {
            sipSender.transmitRequest(sipRequest.getLocalAddress().getHostAddress(), response);
        }

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
     * 从 SIP 请求中解析租户，通过 Redis 路由到租户，设置租户上下文和 SIP 配置。
     * <p>
     * 查找策略：
     * 1. Request-URI sipId → 查 Redis Hash（标准 GB28181: 设备 SIP服务器编号 = 平台 sipId）
     * 2. FROM header 设备编号 → 查 Redis Hash（兼容: 部分设备 SIP服务器编号填非标值）
     * 3. SIP 域匹配：设备编号前10位 = sipDomain
     * 4. 全局只有一个配置时直接命中
     */
    public void handlerTenantId(SIPRequest sipRequest) {
        // 从 Request-URI 提取 SIP 服务器编号
        SipUri requestUri = (SipUri) sipRequest.getRequestLine().getUri();
        String requestSipId = requestUri.getAuthority().getUser();

        // 从 FROM header 提取设备编号
        String deviceIdentification = SipUtils.getUserIdFromFromHeader(sipRequest);

        // 1. Request-URI sipId 精确匹配
        TenantSipConfigCacheVO cacheVO = null;
        if (StrUtil.isNotBlank(requestSipId)) {
            cacheVO = tenantSipConfigProvider.resolveBySipId(requestSipId).orElse(null);
        }

        // 2. FROM header 设备编号查 Redis（设备编号可能与平台 sipId 在同一域）
        if (cacheVO == null && StrUtil.isNotBlank(deviceIdentification)) {
            cacheVO = tenantSipConfigProvider.resolveBySipId(deviceIdentification).orElse(null);
        }

        // 3+4. 回退: SIP 域匹配 / 单配置直接命中
        if (cacheVO == null) {
            cacheVO = tenantSipConfigProvider.resolveByFuzzyMatch(requestSipId, deviceIdentification).orElse(null);
        }

        if (cacheVO == null) {
            throw new BizException("未找到租户配置 [requestSipId=" + requestSipId
                    + ", device=" + deviceIdentification + "]，请检查 SIP 配置和设备端配置");
        }

        // 校验：设备发往的 requestSipId（= 设备配置里写的"平台 SIP ID"）必须和 video_sip_config.sip_id 一致。
        // 不一致的常见诱因：用户把"设备 SIP 编号"误填到了平台 SIP 配置的 sip_id 字段。
        // 这会让出站 INVITE 的 From URI 用户名等于设备自己的国标编号，设备 200 OK 后立即 BYE 拆 dialog。
        if (StrUtil.isNotBlank(requestSipId) && !requestSipId.equals(cacheVO.getSipId())) {
            log.warn("[平台 SIP ID 不一致] 设备 {} 发往 requestSipId={}, 但 video_sip_config.sip_id={}。"
                            + "出站 INVITE 的 From URI 用户名将使用 {}，设备很可能拒绝（200 OK 后立即 BYE）。"
                            + "请在『流媒体 / 平台 SIP 配置』里把 sip_id 改成 {}（与设备端配置的'平台 SIP 编号'一致）。",
                    deviceIdentification, requestSipId, cacheVO.getSipId(),
                    cacheVO.getSipId(), requestSipId);
        }

        // 校验：设备编号不能与平台 SIP 服务器编号相同。
        // 摄像头出厂默认会把"SIP 服务器编号"和"设备编号"都设成同一个 20 位国标编号（如大华 IPC 默认 44010200492000000001），
        // 用户没改就直接接入，会导致出站 INVITE 的 From URI 用户名 = 设备自己的 deviceId，设备 SIP 栈视为"自己给自己"
        // 拒绝 dialog（200 OK 后立即 BYE，前端无限转圈）。这里在 SIP 入站路由阶段就提示，方便排障。
        if (StrUtil.isNotBlank(deviceIdentification) && deviceIdentification.equals(cacheVO.getSipId())) {
            log.error("[配置冲突] 设备编号 {} 与平台「SIP 服务器编号」相同。GB28181 不允许这种配置，"
                            + "设备会在 INVITE 200 OK 后立即 BYE，前端表现为一直转圈。"
                            + "请进入设备 GB28181 配置页面，把「设备编号」改成与「SIP 服务器编号」不同的 20 位国标编号。",
                    deviceIdentification);
        }

        // 设置租户上下文 + ThreadLocal 缓存
        ContextUtil.setTenantId(String.valueOf(cacheVO.getTenantId()));
        TenantSipConfigHolder.set(TenantSipConfig.of(cacheVO, sipLayer.getMonitorIp(), sipConfig.getPort()));
        log.info("[租户路由] requestSipId={}, device={}, 命中sipId={}, tenantId={}",
                requestSipId, deviceIdentification, cacheVO.getSipId(), cacheVO.getTenantId());
    }

    class ResponseAckExtraParam {
        String content;
        ContentTypeHeader contentTypeHeader;
        SipURI sipURI;
        int expires = -1;
    }


}
