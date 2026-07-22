package com.mqttsnet.thinglinks.protocol.vo.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * -----------------------------------------------------------------------------
 * 文件名称: CommandIssueRequestParam.java
 * -----------------------------------------------------------------------------
 * 描述:
 * Platform Command Issue Request Data Model
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * 修改历史:
 * 日期           作者          版本        描述
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-10-17 09:48
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Builder
@Schema(title = "CommandIssueRequestParam", description = "Device Command Request Data Structure")
public class CommandIssueRequestParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Device unique identifier, ALL for all devices.")
    @NotEmpty(message = "Device ID cannot be empty")
    private String deviceIdentification;

    @Schema(description = "Product unique identifier.")
    @NotEmpty(message = "Product ID cannot be empty")
    private String productIdentification;

    @Schema(description = "Fixed value 'cloudReq', indicating the request issued by the platform.")
    @NotEmpty(message = "Message type cannot be empty")
    private String msgType;

    @Schema(description = "Service code.")
    @NotEmpty(message = "Service code cannot be empty")
    private String serviceCode;

    @Schema(description = "Command code for the service.")
    @NotEmpty(message = "Command cannot be empty")
    private String cmd;

    @Schema(description = "Parameters for the command.")
    @NotNull(message = "Parameters cannot be null")
    private Map<String, Object> params;

    @Schema(description = "Product version no. the structured command is built against (optional, for record/trace).")
    private String versionNo;
}
