package com.mqttsnet.thinglinks.video.gb28181.transmit.response;

import javax.sip.ResponseEvent;

/**
 * @description: 处理接收IPCamera发来的SIP协议响应消息
 * @author: mqttsnet
 */
public interface ISIPResponseProcessor {


    void process(ResponseEvent evt);


}
