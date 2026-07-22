package com.mqttsnet.thinglinks.video.gb28181.transmit.callback;

import lombok.Data;

/**
 * @author: mqttsnet
 */
@Data
public class RequestMessage {

    private String id;

    private String key;

    private Object data;
}
