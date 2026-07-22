package com.mqttsnet.thinglinks.dto.ws;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.mqttsnet.thinglinks.enumeration.WsMessageTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mqttsnet
 * @since 2021/3/23
 */
@Data
@Slf4j
public class WebSocketMessageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 消息类型
     */
    private WsMessageTypeEnum wsMessageTypeEnum;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 客户端所连接的服务端的连接数量
     */
    private Integer serverUserCount;

    /**
     * 客户端所连接的服务端的局域网 IP，或唯一标识
     */
    private String serverIp;

    private LocalDateTime sendTime;
}
