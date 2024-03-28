package com.mqttsnet.thinglinks.link.api.domain.device.vo.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * -----------------------------------------------------------------------------
 * File Name: DeviceShadowPageQuery
 * -----------------------------------------------------------------------------
 * Description:
 * 设备影子查询参数
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/3/26       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2024/3/26 19:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@ApiModel(value = "DeviceShadowPageQuery", description = "设备影子信息分页参数")
public class DeviceShadowPageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备标识
     */
    @ApiModelProperty(name = "deviceIdentification", value = "设备标识", required = true, example = "7939700746264577", notes = "必填参数，用于标识需要查询的设备。")
    private String deviceIdentification;

    /**
     * 开始时间
     */
    @ApiModelProperty(name = "startTime", value = "开始时间戳", example = "1622552643000", notes = "选填参数，用于指定查询的起始时间。格式：13位毫秒时间戳。例如，1622552643000表示2021年6月1日17时24分3秒（UTC时间）。")
    private Long startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(name = "endTime", value = "结束时间戳", example = "1622552644000", notes = "选填参数，用于指定查询的结束时间。格式：13位毫秒时间戳。例如，1622552644000表示2021年6月1日17时24分4秒（UTC时间）。")
    private Long endTime;
}

