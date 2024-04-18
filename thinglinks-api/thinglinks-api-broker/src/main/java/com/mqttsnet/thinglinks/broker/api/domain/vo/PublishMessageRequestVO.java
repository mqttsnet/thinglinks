package com.mqttsnet.thinglinks.broker.api.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

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
public class PublishMessageRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "可选的调用者提供的请求ID", example = "1234567890")
    private Long reqId;

    @ApiModelProperty(value = "租户ID", required = true, example = "thinglinks")
    private String tenantId;

    @ApiModelProperty(value = "消息主题", required = true, example = "exampleTopic")
    private String topic;

    @ApiModelProperty(value = "QoS of the message to be published", required = true, example = "1")
    private String qos;

    @ApiModelProperty(value = "消息过期秒数", example = "3600")
    private String expirySeconds;

    @ApiModelProperty(value = "发布者类型", required = true, example = "web")
    private String clientType;

    @ApiModelProperty(value = "关于发布者的元数据头，必须以client_meta_开头", example = "client_meta_exampleKey: value")
    private Map<String, String> clientMetadata;

    @ApiModelProperty(value = "消息负载，将作为二进制处理", required = true)
    private String payload;

}
