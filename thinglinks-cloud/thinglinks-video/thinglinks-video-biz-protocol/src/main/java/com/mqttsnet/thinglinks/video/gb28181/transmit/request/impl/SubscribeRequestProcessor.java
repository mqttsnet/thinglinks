package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl;

import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.SipCommandTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.event.subscribe.SubscribeHolder;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.gb28181.transmit.observer.SIPProcessorObserver;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.ISIPRequestProcessor;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.header.ExpiresHeader;
import javax.sip.message.Response;
import java.text.ParseException;

/**
 * SIP命令类型： SUBSCRIBE请求
 *
 * @author mqttnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {

    private final String method = SipCommandTypeEnum.SUBSCRIBE.getValue();

    private final SIPProcessorObserver sipProcessorObserver;

    private final SubscribeHolder subscribeHolder;

    private final SIPSender sipSender;


    @Override
    public void afterPropertiesSet() throws Exception {
        // 添加消息处理的订阅
        sipProcessorObserver.addRequestProcessor(method, this);
    }

    /**
     * 处理SUBSCRIBE请求
     *
     * @param evt 事件
     */
    @Override
    public void process(RequestEvent evt) {
        SIPRequest request = (SIPRequest) evt.getRequest();
        handlerTenantId(request);
        try {
            Element rootElement = getRootElement(evt);
            if (rootElement == null) {
                log.error("处理SUBSCRIBE请求  未获取到消息体{}", evt.getRequest());
                responseAck(request, Response.BAD_REQUEST);
                return;
            }
            String cmd = XmlUtil.getText(rootElement, "CmdType");
            if (CmdTypeEnum.MOBILE_POSITION.getValue().equals(cmd)) {
                processNotifyMobilePosition(request, rootElement);
            } else if (CmdTypeEnum.CATALOG.getValue().equals(cmd)) {
                processNotifyCatalogList(request, rootElement);
            } else {
                log.info("接收到消息：" + cmd);

                Response response = getMessageFactory().createResponse(200, request);
                if (response != null) {
                    ExpiresHeader expireHeader = getHeaderFactory().createExpiresHeader(30);
                    response.setExpires(expireHeader);
                }
                log.info("response : " + response);
                sipSender.transmitRequest(request.getLocalAddress().getHostAddress(), response);
            }
        } catch (ParseException | SipException | InvalidArgumentException | DocumentException e) {
            log.error("未处理的异常 ", e);
        }

    }

    /**
     * 处理移动位置订阅消息
     */
    private void processNotifyMobilePosition(SIPRequest request, Element rootElement) throws SipException {
        if (request == null) {
            return;
        }
        String platformId = SipUtils.getUserIdFromFromHeader(request);
        String deviceIdentification = XmlUtil.getText(rootElement, "DeviceID");

        String sn = XmlUtil.getText(rootElement, "SN");
        log.info("[回复上级的移动位置订阅请求]: {}", platformId);
        StringBuilder resultXml = new StringBuilder(200);
        resultXml.append("<?xml version=\"1.0\" ?>\r\n")
                .append("<Response>\r\n")
                .append("<CmdType>MobilePosition</CmdType>\r\n")
                .append("<SN>").append(sn).append("</SN>\r\n")
                .append("<DeviceID>").append(deviceIdentification).append("</DeviceID>\r\n")
                .append("<Result>OK</Result>\r\n")
                .append("</Response>\r\n");

    }

    private void processNotifyCatalogList(SIPRequest request, Element rootElement) throws SipException {
        if (request == null) {
            return;
        }
        String platformId = SipUtils.getUserIdFromFromHeader(request);
        String deviceIdentification = XmlUtil.getText(rootElement, "DeviceID");
        log.info("[回复上级的目录订阅请求]: {}/{}", platformId, deviceIdentification);
    }
}
