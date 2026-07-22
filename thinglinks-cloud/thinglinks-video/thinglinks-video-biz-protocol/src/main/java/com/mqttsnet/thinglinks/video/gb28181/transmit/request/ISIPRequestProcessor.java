package com.mqttsnet.thinglinks.video.gb28181.transmit.request;

import javax.sip.RequestEvent;

/**
 * @description: 对SIP事件进行处理，包括request， response， timeout， ioException, transactionTerminated,dialogTerminated
 * @author: mqttsnet
 */
public interface ISIPRequestProcessor {

    void process(RequestEvent event);

}
