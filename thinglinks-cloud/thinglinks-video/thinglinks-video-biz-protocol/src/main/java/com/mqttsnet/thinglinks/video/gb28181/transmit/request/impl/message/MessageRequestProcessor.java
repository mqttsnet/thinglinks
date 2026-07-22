package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message;

import cn.hutool.core.util.IdUtil;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.dto.gb28181.event.DeviceNotFoundEvent;
import com.mqttsnet.thinglinks.video.dto.gb28181.sip.SsrcTransaction;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.SipCommandTypeEnum;

import com.mqttsnet.thinglinks.video.gb28181.event.subscribe.SipSubscribe;
import com.mqttsnet.thinglinks.video.manager.stream.SipInviteSessionManager;
import com.mqttsnet.thinglinks.video.gb28181.transmit.observer.SIPProcessorObserver;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.ISIPRequestProcessor;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.utils.gb28181.SipUtils;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * message 请求消息处理
 *
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2025/5/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {

    private static final Map<String, IMessageHandler> MESSAGE_HANDLER_MAP = new ConcurrentHashMap<>();
    public final String method = SipCommandTypeEnum.MESSAGE.getValue();
    private final SIPProcessorObserver sipProcessorObserver;

    private final SipSubscribe sipSubscribe;

    private final SipInviteSessionManager sessionManager;

    private final VideoCacheDataHelper videoCacheDataHelper;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 添加消息处理的订阅
        sipProcessorObserver.addRequestProcessor(method, this);
    }

    public void addHandler(String name, IMessageHandler handler) {
        MESSAGE_HANDLER_MAP.put(name, handler);
    }

    @Override
    public void process(RequestEvent evt) {
        MDC.put(ContextConstants.TRACE_ID_HEADER, IdUtil.fastSimpleUUID());
        log.info("接收到设备[{}]消息：{}", method, evt.getRequest());
        SIPRequest sipRequest = (SIPRequest) evt.getRequest();
        handlerTenantId(sipRequest);
        String deviceIdentification = SipUtils.getUserIdFromFromHeader(evt.getRequest());
        CallIdHeader callIdHeader = sipRequest.getCallIdHeader();
        CSeqHeader cSeqHeader = sipRequest.getCSeqHeader();

        // 先从会话内查找
        SsrcTransaction ssrcTransaction = sessionManager.getSsrcTransactionByCallId(deviceIdentification, callIdHeader.getCallId());
        // 兼容海康 媒体通知 消息from字段不是设备ID的问题
        if (ssrcTransaction != null) {
            deviceIdentification = ssrcTransaction.getDeviceIdentification();
        }
        SIPRequest request = (SIPRequest) evt.getRequest();
        // 查询设备是否存在
        VideoDeviceResultVO deviceInfo = BeanPlusUtil.toBeanIgnoreError(videoCacheDataHelper.getDeviceInfo(deviceIdentification), VideoDeviceResultVO.class);

        VideoPlatformInfo parentVideoPlatformInfo = null;
        try {
            if (Objects.isNull(deviceInfo)) {
                // 不存在则回复404
                responseAck(request, Response.NOT_FOUND, "device:" + deviceIdentification + " not found");
                log.warn("[设备未找到]deviceIdentification: {}, callId: {}", deviceIdentification, callIdHeader.getCallId());
                String subscribeKey = callIdHeader.getCallId() + cSeqHeader.getSeqNumber();
                if (sipSubscribe.hasSubscribe(subscribeKey)) {
                    DeviceNotFoundEvent deviceNotFoundEvent = new DeviceNotFoundEvent(callIdHeader.getCallId());
                    SipSubscribe.EventResult eventResult = new SipSubscribe.EventResult(deviceNotFoundEvent);
                    sipSubscribe.complete(subscribeKey, eventResult);
                }
            } else {
                Element rootElement;
                try {
                    rootElement = getRootElement(evt);
                    if (rootElement == null) {
                        log.error("处理MESSAGE请求  未获取到消息体{}", evt.getRequest());
                        responseAck(request, Response.BAD_REQUEST, "MESSAGE请求 content is null");
                        return;
                    }
                    String name = rootElement.getName();
                    IMessageHandler messageHandler = MESSAGE_HANDLER_MAP.get(name);
                    if (messageHandler != null) {
                        if (Objects.nonNull(deviceInfo)) {
                            messageHandler.handForDevice(evt, deviceInfo, rootElement);
                        } else {
                            // 由于上面已经判断都为null则直接返回，所以这里device和parentPlatform必有一个不为null
                            messageHandler.handForPlatform(evt, parentVideoPlatformInfo, rootElement);
                        }
                    } else {
                        // 不支持的message
                        // 不存在则回复415
                        responseAck(request, Response.UNSUPPORTED_MEDIA_TYPE, "Unsupported message type, must Control/Notify/Query/Response");
                    }
                } catch (DocumentException e) {
                    log.warn("解析XML消息内容异常", e);
                    // 不存在则回复404
                    responseAck(request, Response.BAD_REQUEST, e.getMessage());
                }
            }


        } catch (SipException e) {
            log.warn("SIP 回复错误", e);
        } catch (InvalidArgumentException e) {
            log.warn("参数无效", e);
        } catch (ParseException e) {
            log.warn("SIP回复时解析异常", e);
        }
    }

}
