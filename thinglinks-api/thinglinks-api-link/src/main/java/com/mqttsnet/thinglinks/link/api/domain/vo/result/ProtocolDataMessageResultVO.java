package com.mqttsnet.thinglinks.link.api.domain.vo.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @program: thinglinks
 * @description: 协议数据内容返回VO
 * @packagename: com.mqttsnet.thinglinks.device.vo.result
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-22 15:44
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
@ApiModel(value = "ProtocolDataMessageResultVO", description = "协议数据内容返回VO")
public class ProtocolDataMessageResultVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "消息头部")
    private Head head;

    @ApiModelProperty(value = "报文体")
    private T dataBody;

    @ApiModelProperty(value = "数据签名")
    private String dataSign;

    @ApiModel(description = "消息头部实体类")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Head {
        @ApiModelProperty(value = "加密标志，0-不加密；1-SM4；2-AES", allowableValues = "0, 1, 2", example = "0")
        private Integer cipherFlag;

        @ApiModelProperty(value = "消息ID（从1自增即可）", example = "3342")
        private Long mid;

        @ApiModelProperty(value = "报文发送时间戳（毫秒）", example = "1624982406963")
        private Long timeStamp;
    }
}
