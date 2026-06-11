package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl;

import cn.hutool.core.util.IdUtil;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.SipCommandTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.transmit.observer.SIPProcessorObserver;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.ISIPRequestProcessor;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
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
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SIP NOTIFY 请求处理器。
 * 处理设备主动发送的 NOTIFY 请求，根据 XML 消息体中的根元素名称分发到对应的消息处理器。
 *
 * GB/T 28181-2016 Section 9.4: NOTIFY 请求用于事件通知，
 * 包括设备保活、报警通知、移动位置通知、目录变更通知等。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {

    public final String method = SipCommandTypeEnum.NOTIFY.getValue();

    private static final Map<String, IMessageHandler> NOTIFY_HANDLER_MAP = new ConcurrentHashMap<>();

    private final SIPProcessorObserver sipProcessorObserver;

    private final VideoCacheDataHelper videoCacheDataHelper;

    @Override
    public void afterPropertiesSet() {
        sipProcessorObserver.addRequestProcessor(method, this);
    }

    /**
     * 注册消息处理器
     *
     * @param name    XML 根元素名称（如 Notify、Query 等）
     * @param handler 消息处理器
     */
    public void addHandler(String name, IMessageHandler handler) {
        NOTIFY_HANDLER_MAP.put(name, handler);
    }

    @Override
    public void process(RequestEvent evt) {
        MDC.put(ContextConstants.TRACE_ID_HEADER, IdUtil.fastSimpleUUID());
        SIPRequest request = (SIPRequest) evt.getRequest();
        handlerTenantId(request);

        String deviceIdentification = SipUtils.getUserIdFromFromHeader(request);
        log.info("[收到NOTIFY] 设备: {}", deviceIdentification);

        try {
            // 查询设备信息
            VideoDeviceResultVO deviceInfo = BeanPlusUtil.toBeanIgnoreError(
                    videoCacheDataHelper.getDeviceInfo(deviceIdentification), VideoDeviceResultVO.class);

            if (Objects.isNull(deviceInfo)) {
                responseAck(request, Response.NOT_FOUND, "device:" + deviceIdentification + " not found");
                log.warn("[设备未找到] NOTIFY请求, deviceIdentification: {}", deviceIdentification);
                return;
            }

            // 解析 XML
            Element rootElement;
            try {
                rootElement = getRootElement(evt);
                if (rootElement == null) {
                    log.error("[NOTIFY] 未获取到消息体: {}", evt.getRequest());
                    responseAck(request, Response.BAD_REQUEST, "NOTIFY content is null");
                    return;
                }
            } catch (DocumentException e) {
                log.warn("[NOTIFY] 解析XML异常", e);
                responseAck(request, Response.BAD_REQUEST, e.getMessage());
                return;
            }

            // 根据根元素名称分发到对应处理器
            String rootName = rootElement.getName();
            IMessageHandler handler = NOTIFY_HANDLER_MAP.get(rootName);
            if (handler != null) {
                handler.handForDevice(evt, deviceInfo, rootElement);
            } else {
                log.warn("[NOTIFY] 未找到消息处理器: rootElement={}", rootName);
                responseAck(request, Response.OK);
            }
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[NOTIFY回复失败] 设备: {}, 错误: {}", deviceIdentification, e.getMessage());
        }
    }
}
