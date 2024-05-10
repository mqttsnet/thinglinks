package com.mqttsnet.thinglinks.link.api.domain.device.vo.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * -----------------------------------------------------------------------------
 * File Name: PublishMessageRequestParam
 * -----------------------------------------------------------------------------
 * Description:
 * 发布消息
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/4/17       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/4/17 19:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@ApiModel(value = "PublishMessageRequestParam", description = "Parameters required for publishing a message via MQTT.")
public class PublishMessageRequestParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "MQTT topic to which the message will be published.", notes = "This is the topic path under which the message will be categorized.")
    @NotEmpty(message = "MQTT topic cannot be empty")
    private String topic;

    @ApiModelProperty(value = "Quality of Service for the MQTT message.", notes = "0 = At most once, 1 = At least once, 2 = Exactly once.")
    @NotEmpty(message = "QoS cannot be empty")
    private String qos;

    @ApiModelProperty(value = "The actual message payload to be sent.", notes = "This payload could be any format as required by the application, encoded appropriately.")
    @NotNull(message = "Message payload cannot be null")
    private String payload;

    @ApiModelProperty(value = "Tenant ID associated with the message, for multi-tenant environments.", notes = "Unique identifier for the tenant.", name = "tenantId ,default value is thinglinks")
    @NotEmpty(message = "Tenant ID cannot be empty")
    private String tenantId;

    @ApiModelProperty(value = "Expiry seconds for the message.", notes = "Duration in seconds after which the message should expire.")
    private String expirySeconds;

    @ApiModelProperty(value = "Additional metadata associated with the message.", notes = "Optional metadata to accompany the message.")
    private Map<String, String> metadata;
}
