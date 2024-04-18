package com.mqttsnet.thinglinks.link.api.domain.device.vo.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * -----------------------------------------------------------------------------
 * File Name: OpenAiChatRequestParam
 * -----------------------------------------------------------------------------
 * Description:
 * OpenAiChatRequest
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2023/12/12       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023/12/12 23:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@ApiModel(value = "OpenAiChatResponseResultVO", description = "Response data from OpenAI Chat API")
public class OpenAiChatResponseResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Unique identifier for the response")
    private String id;

    @ApiModelProperty(value = "Type of the object returned")
    private String object;

    @ApiModelProperty(value = "Timestamp of creation")
    private long created;

    @ApiModelProperty(value = "Model used for generating the response")
    private String model;

    @ApiModelProperty(value = "List of choices provided by OpenAI")
    private List<ChatChoice> choices;

    @ApiModelProperty(value = "Usage information of tokens")
    private Usage usage;

    @ApiModelProperty(value = "Any warning returned by the API")
    private String warning;

    @ApiModelProperty(value = "System fingerprint")
    @JsonProperty("system_fingerprint")
    private String systemFingerprint;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Usage implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "Number of tokens used in the prompt")
        @JsonProperty("prompt_tokens")
        private long promptTokens;

        @ApiModelProperty(value = "Number of tokens used in the completion")
        @JsonProperty("completion_tokens")
        private long completionTokens;

        @ApiModelProperty(value = "Total number of tokens used")
        @JsonProperty("total_tokens")
        private long totalTokens;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class ChatChoice implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "Index of the choice")
        private long index;

        @ApiModelProperty(value = "Message content in delta format (when stream is true)")
        @JsonProperty("delta")
        private Message delta;

        @ApiModelProperty(value = "Message content (when stream is false)")
        @JsonProperty("message")
        private Message message;

        @ApiModelProperty(value = "Reason for completion")
        @JsonProperty("finish_reason")
        private String finishReason;


        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @ToString
        @EqualsAndHashCode
        public static class Message implements Serializable {
            private static final long serialVersionUID = 1L;

            @ApiModelProperty(value = "Content of the message")
            private String content;

            @ApiModelProperty(value = "Role of the message sender (e.g., 'user', 'system')")
            private String role;
        }
    }

}
