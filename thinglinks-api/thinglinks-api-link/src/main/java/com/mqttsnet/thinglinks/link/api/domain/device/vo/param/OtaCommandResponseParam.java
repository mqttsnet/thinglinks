package com.mqttsnet.thinglinks.link.api.domain.device.vo.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * -----------------------------------------------------------------------------
 * File Name: OtaCommandResponseParam
 * -----------------------------------------------------------------------------
 * Description:
 * OTA 远程升级响应参数
 * 用于设备向物联网平台上报 OTA 升级结果
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/1/18       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2024/1/18 22:28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "OtaCommandResponseParam", description = "Response Data Structure for OTA Command")
public class OtaCommandResponseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "The unique identifier of the device.")
    private String deviceIdentification;

    @ApiModelProperty(value = "The unique identifier of the OTA task.")
    private Long otaTaskId;

    @ApiModelProperty(value = "The status of the OTA upgrade: 0 for pending, 1 for in progress, 2 for success, 3 for failure.")
    private Integer upgradeStatus;

    @ApiModelProperty(value = "The progress of the OTA upgrade in percentage.")
    private Integer progress;

    @ApiModelProperty(value = "The start time of the OTA upgrade as a timestamp.")
    private Long startTime;

    @ApiModelProperty(value = "The end time of the OTA upgrade as a timestamp.")
    private Long endTime;

    @ApiModelProperty(value = "The error code, if any, resulting from the OTA upgrade.")
    private String errorCode;

    @ApiModelProperty(value = "The error message, if any, resulting from the OTA upgrade.")
    private String errorMessage;

    @ApiModelProperty(value = "Details about the success of the OTA upgrade.")
    private String successDetails;

    @ApiModelProperty(value = "Details about the failure of the OTA upgrade.")
    private String failureDetails;

    @ApiModelProperty(value = "Logs detailing the OTA upgrade process.")
    private String logDetails;
}
