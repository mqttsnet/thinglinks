package com.mqttsnet.basic.protocol.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * -----------------------------------------------------------------------------
 * File Name: ProtocolDataMessageDto.java
 * -----------------------------------------------------------------------------
 * Description:
 * ProtocolDataMessage
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-11-12 01:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
@ApiModel(value = "ProtocolDataMessageDto", description = "协议数据内容Dto")
public class ProtocolDataMessageDTO<T> implements Serializable {

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
