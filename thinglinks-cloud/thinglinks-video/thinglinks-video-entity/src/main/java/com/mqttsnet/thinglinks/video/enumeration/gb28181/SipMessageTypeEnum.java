package com.mqttsnet.thinglinks.video.empowerment.gb28181;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * SIP 服务器消息 类型
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2025/5/25
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "SipMessageTypeEnum", description = "SIP 消息类型枚举")
public enum SipMessageTypeEnum {

    QUERY("Query", "查询"),
    RESPONSE("Response", "响应"),
    ;


    private String value;
    private String desc;


    /**
     * 根据value获取对应的枚举
     *
     * @param value 产生源类型的标识
     * @return 返回对应的枚举，如果没找到则返回 Optional.empty()
     */
    public static Optional<SipMessageTypeEnum> fromValue(String value) {
        return Stream.of(SipMessageTypeEnum.values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst();
    }


}
