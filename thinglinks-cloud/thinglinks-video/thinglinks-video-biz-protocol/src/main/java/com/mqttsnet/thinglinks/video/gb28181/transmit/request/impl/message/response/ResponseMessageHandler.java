package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response;


import com.mqttsnet.thinglinks.video.dto.device.VideoDeviceInfoResultDTO;
import com.mqttsnet.thinglinks.video.empowerment.gb28181.ErrorCodeEnum;
import com.mqttsnet.thinglinks.video.empowerment.gb28181.SipMessageTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.event.sip.MessageEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.subscribe.MessageSubscribe;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.MessageHandlerAbstract;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.MessageRequestProcessor;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.RequestEvent;

import static com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil.getText;

/**
 * 命令类型： 请求动作的应答
 * 命令类型： 设备控制, 报警通知, 设备目录信息查询, 目录信息查询, 目录收到, 设备信息查询, 设备状态信息查询 ......
 */
@Component
public class ResponseMessageHandler extends MessageHandlerAbstract implements InitializingBean {

    private final String messageType = SipMessageTypeEnum.RESPONSE.getValue();

    @Autowired
    private MessageRequestProcessor messageRequestProcessor;

    @Autowired
    private MessageSubscribe messageSubscribe;

    @Override
    public void afterPropertiesSet() throws Exception {
        messageRequestProcessor.addHandler(messageType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceInfoResultDTO deviceInfo, Element element) {
        super.handForDevice(evt, deviceInfo, element);
        handMessageEvent(element, null);
    }

    public void handMessageEvent(Element element, Object data) {
        String cmd = getText(element, "CmdType");
        String sn = getText(element, "SN");
        MessageEvent<Object> subscribe = (MessageEvent<Object>) messageSubscribe.getSubscribe(cmd + sn);
        if (subscribe != null && subscribe.getCallback() != null) {
            String result = getText(element, "Result");
            if (result == null || "OK".equalsIgnoreCase(result) || data != null) {
                subscribe.getCallback().run(ErrorCodeEnum.SUCCESS.getCode(), ErrorCodeEnum.SUCCESS.getMsg(), data);
            } else {
                subscribe.getCallback().run(ErrorCodeEnum.ERROR100.getCode(), ErrorCodeEnum.ERROR100.getMsg(), result);
            }
            messageSubscribe.removeSubscribe(cmd + sn);
        }
    }
}
