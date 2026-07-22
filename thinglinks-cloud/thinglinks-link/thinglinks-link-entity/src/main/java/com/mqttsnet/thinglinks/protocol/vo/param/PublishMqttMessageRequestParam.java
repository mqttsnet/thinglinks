package com.mqttsnet.thinglinks.protocol.vo.param;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.codec.Base64;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * -----------------------------------------------------------------------------
 * File Name: PublishMessageRequestParam
 * -----------------------------------------------------------------------------
 * Description:
 * 发布消息- 支持文本和二进制数据
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
 * @date 2024/4/17 18:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Schema(title = "PublishMqttMessageRequestParam", description = "Parameters required for publishing a message via MQTT.")
public class PublishMqttMessageRequestParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "MQTT topic to which the message will be published.")
    @NotEmpty(message = "MQTT topic cannot be empty")
    private String topic;

    @Schema(description = "Quality of Service for the MQTT message.")
    @NotEmpty(message = "QoS cannot be empty")
    private String qos;

    @Schema(
            description = "Message payload content. Supported formats:\n" +
                    "- Plain text: Direct string input, e.g., 'Hello World'\n" +
                    "- JSON data: JSON format string\n" +
                    "- Binary data: Must be Base64 encoded string, e.g., 'SGVsbG8gV29ybGQ='\n" +
                    "Note: Binary data must be Base64 encoded before transmission",
            example = "Hello World or SGVsbG8gV29ybGQ=",
            extensions = {
                    @Extension(name = "x-examples", properties = {
                            @ExtensionProperty(name = "Plain Text", value = "Hello World"),
                            @ExtensionProperty(name = "JSON Data", value = "{\"sensor\":\"temperature\",\"value\":25.5}"),
                            @ExtensionProperty(name = "Base64 Binary", value = "SGVsbG8gV29ybGQ=")
                    })
            }
    )
    @NotNull(message = "Message payload cannot be null")
    private Object payload;

    @Schema(description = "Tenant ID associated with the message, for multi-tenant environments.")
    @NotEmpty(message = "Tenant ID cannot be empty")
    private String tenantId;

    @Schema(description = "Expiry seconds for the message.")
    private String expirySeconds;

    @Schema(description = "Additional metadata associated with the message.")
    private Map<String, String> metadata;

    @Schema(description = "Device identification to attribute this downlink to (optional, for command history).")
    private String deviceIdentification;

    /**
     * 获取智能编码的负载字符串
     * - 字节数组：Base64编码
     * - 字符串：如果是Base64格式保持原样，否则直接使用
     */
    public String getPayloadAsSmartString() {
        if (payload == null) {
            return null;
        }

        if (payload instanceof byte[]) {
            // 字节数组直接Base64编码
            return Base64.encode((byte[]) payload);
        } else if (payload instanceof String) {
            String strPayload = (String) payload;
            // 如果是Base64格式，直接使用
            if (Base64.isBase64(strPayload)) {
                return strPayload;
            } else {
                // 普通字符串直接返回
                return strPayload;
            }
        } else {
            // 其他类型转为字符串
            return payload.toString();
        }
    }

    /**
     * 检测负载是否为Base64格式
     */
    public boolean isPayloadBase64() {
        if (payload == null) {
            return false;
        }

        if (payload instanceof String) {
            return Base64.isBase64((String) payload);
        } else if (payload instanceof byte[]) {
            // 字节数组需要Base64编码
            return true;
        } else {
            return false;
        }
    }
}
