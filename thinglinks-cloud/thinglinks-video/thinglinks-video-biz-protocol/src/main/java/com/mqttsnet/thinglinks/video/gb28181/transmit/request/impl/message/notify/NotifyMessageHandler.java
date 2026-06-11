package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.notify;

import com.mqttsnet.thinglinks.video.enumeration.gb28181.SipMessageTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.MessageHandlerAbstract;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.MessageRequestProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * NOTIFY 消息分发处理器。
 * 注册到 {@link MessageRequestProcessor} 中处理 MESSAGE 请求中消息类型为 Notify 的消息。
 * 根据 CmdType 分发到具体的子处理器：
 * <ul>
 *   <li>Keepalive - 设备保活</li>
 *   <li>Alarm - 报警通知</li>
 *   <li>MobilePosition - 移动位置通知</li>
 *   <li>Catalog - 目录变更通知</li>
 * </ul>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Component
public class NotifyMessageHandler extends MessageHandlerAbstract implements InitializingBean {

    private final String messageType = SipMessageTypeEnum.NOTIFY.getValue();

    @Autowired
    private MessageRequestProcessor messageRequestProcessor;

    @Override
    public void afterPropertiesSet() throws Exception {
        messageRequestProcessor.addHandler(messageType, this);
    }
}
