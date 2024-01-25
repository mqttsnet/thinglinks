package com.mqttsnet.thinglinks.broker.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @program: thinglinks
 * @description: MQTT 发送消息VO
 * @packagename: com.mqttsnet.thinglinks.broker.api.domain
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2024-01-20 19:11
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@ApiModel(value = "PublishMessageRequestVO", description = "MQTT 发送消息VO")
public class PublishMessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "可选的调用者提供的请求ID", example = "1234567890")
    private Long reqId;

    @ApiModelProperty(value = "租户ID", required = true, example = "tenant12345")
    private String tenantId;

    @ApiModelProperty(value = "消息主题", required = true, example = "exampleTopic")
    private String topic;

    @ApiModelProperty(value = "QoS of the message to be distributed", required = true)
    private String pubQos;

    @ApiModelProperty(value = "客户端类型", required = true, example = "web")
    private String clientType;

    @ApiModelProperty(value = "消息是否应保留", example = "true")
    private String retain;

    @ApiModelProperty(value = "关于kicker客户端的元数据头，必须以client_meta_开头", example = "client_meta_exampleKey")
    private String clientMeta;

    @ApiModelProperty(value = "payload", required = true)
    private String payload;
}
