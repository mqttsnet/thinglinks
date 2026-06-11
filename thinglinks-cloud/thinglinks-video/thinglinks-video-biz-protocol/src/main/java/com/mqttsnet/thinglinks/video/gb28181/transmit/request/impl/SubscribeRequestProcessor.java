package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl;

import com.mqttsnet.thinglinks.video.empowerment.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.empowerment.gb28181.SipCommandTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.event.subscribe.SubscribeHolder;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.gb28181.transmit.observer.SIPProcessorObserver;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.ISIPRequestProcessor;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SubscribeRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {

    private final String method = SipCommandTypeEnum.SUBSCRIBE.getValue();

    @Autowired
    private SIPProcessorObserver sipProcessorObserver;

    @Autowired
    private SubscribeHolder subscribeHolder;

    @Autowired
    private SIPSender sipSender;


//	@Autowired
//	private IPlatformService platformService;

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
//			} else if (CmdType.ALARM.equals(cmd)) {
//				logger.info("接收到Alarm订阅");
//				processNotifyAlarm(serverTransaction, rootElement);
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
        String deviceId = XmlUtil.getText(rootElement, "DeviceID");
		/*VideoPlatformInfo platform = platformService.queryPlatformByServerGBId(platformId);
		SubscribeInfo subscribeInfo = new SubscribeInfo(request, platformId);
		if (platform == null) {
			return;
		}*/

        String sn = XmlUtil.getText(rootElement, "SN");
        log.info("[回复上级的移动位置订阅请求]: {}", platformId);
        StringBuilder resultXml = new StringBuilder(200);
        resultXml.append("<?xml version=\"1.0\" ?>\r\n")
                .append("<Response>\r\n")
                .append("<CmdType>MobilePosition</CmdType>\r\n")
                .append("<SN>").append(sn).append("</SN>\r\n")
                .append("<DeviceID>").append(deviceId).append("</DeviceID>\r\n")
                .append("<Result>OK</Result>\r\n")
                .append("</Response>\r\n");

        /*if (subscribeInfo.getExpires() > 0) {
            // GPS上报时间间隔
            String interval = XmlUtil.getText(rootElement, "Interval");
            if (interval == null) {
                subscribeInfo.setGpsInterval(5);
            } else {
                subscribeInfo.setGpsInterval(Integer.parseInt(interval));
            }
            subscribeInfo.setSn(sn);
        }

        try {
            SIPResponse response = responseXmlAck(request, resultXml.toString(), platform, subscribeInfo.getExpires());
            if (subscribeInfo.getExpires() == 0) {
                subscribeHolder.removeMobilePositionSubscribe(platformId);
            } else {
                subscribeInfo.setResponse(response);
                subscribeHolder.putMobilePositionSubscribe(platformId, subscribeInfo, () -> {
                    platformService.sendNotifyMobilePosition(platformId);
                });
            }

        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("未处理的异常 ", e);
        }*/
    }

    private void processNotifyAlarm(RequestEvent evt, Element rootElement) {

    }

    private void processNotifyCatalogList(SIPRequest request, Element rootElement) throws SipException {
        if (request == null) {
            return;
        }
        String platformId = SipUtils.getUserIdFromFromHeader(request);
        String deviceId = XmlUtil.getText(rootElement, "DeviceID");
        /*Platform platform = platformService.queryPlatformByServerGBId(platformId);
        if (platform == null) {
            return;
        }
        SubscribeInfo subscribeInfo = new SubscribeInfo(request, platformId);

        String sn = XmlUtil.getText(rootElement, "SN");
        log.info("[回复上级的目录订阅请求]: {}/{}", platformId, deviceId);
        StringBuilder resultXml = new StringBuilder(200);
        resultXml.append("<?xml version=\"1.0\" ?>\r\n")
                .append("<Response>\r\n")
                .append("<CmdType>Catalog</CmdType>\r\n")
                .append("<SN>").append(sn).append("</SN>\r\n")
                .append("<DeviceID>").append(deviceId).append("</DeviceID>\r\n")
                .append("<Result>OK</Result>\r\n")
                .append("</Response>\r\n");

        if (subscribeInfo.getExpires() > 0) {
            subscribeHolder.putCatalogSubscribe(platformId, subscribeInfo);
        } else if (subscribeInfo.getExpires() == 0) {
            subscribeHolder.removeCatalogSubscribe(platformId);
        }
        try {
            VideoPlatformInfo parentPlatform = platformService.queryPlatformByServerGBId(platformId);
            SIPResponse response = responseXmlAck(request, resultXml.toString(), parentPlatform, subscribeInfo.getExpires());
            if (subscribeInfo.getExpires() == 0) {
                subscribeHolder.removeCatalogSubscribe(platformId);
            } else {
                subscribeInfo.setResponse(response);
                subscribeHolder.putCatalogSubscribe(platformId, subscribeInfo);
            }
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("未处理的异常 ", e);
        }
        if (subscribeHolder.getCatalogSubscribe(platformId) == null
                && platform.getAutoPushChannel() != null && platform.getAutoPushChannel()) {
            platformService.addSimulatedSubscribeInfo(platform);
        }*/
    }
}
