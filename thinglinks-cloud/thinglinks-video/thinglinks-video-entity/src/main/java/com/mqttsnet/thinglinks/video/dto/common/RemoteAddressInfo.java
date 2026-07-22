package com.mqttsnet.thinglinks.video.dto.common;


import lombok.Data;

@Data
public class RemoteAddressInfo {
    /**
     * 地址(IP/域名)
     */
    private String host;
    private int port;

    public RemoteAddressInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

}
