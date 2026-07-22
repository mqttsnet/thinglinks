package com.mqttsnet.thinglinks.device.vo.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;


/**
 * 设备影子信息分页查询参数。
 *
 * @author mqttsnet
 * @date 2023-10-14 19:39:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "DeviceShadowPageQuery", description = "设备影子信息分页参数")
public class DeviceShadowPageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备标识
     */
    @Schema(title = "deviceIdentification", description = "必填参数，用于标识需要查询的设备。", requiredMode = Schema.RequiredMode.REQUIRED, example = "7939700746264577")
    private String deviceIdentification;

    /**
     * 开始时间
     */
    @Schema(title = "startTime", description = "选填参数，用于指定查询的起始时间。格式：19位纳秒时间戳。例如，1622552643000000000表示2021年6月1日17时24分3秒（UTC时间）。", example = "1622552643000000000")
    private Long startTime;

    /**
     * 结束时间
     */
    @Schema(title = "endTime", description = "选填参数，用于指定查询的结束时间。格式：19位纳秒时间戳。例如，1622552644000000000表示2021年6月1日17时24分4秒（UTC时间）。", example = "1622552644000000000")
    private Long endTime;

    /**
     * 服务编码
     */
    @Schema(title = "serviceCode", description = "选填参数，用于指定查询的服务编码。若不传，查询产品下所有服务。", example = "serviceCode1")
    private String serviceCode;

    /**
     * 产品版本序号(选填):不传按 device.boundProductVersionNo(设备当前版本)查;传值则按该版本快照解析物模型并拼 TD 子表名,支持回看历史版本影子。
     * TD 子表按 (productIdentification, versionNo, deviceIdentification) 拼名,老版本须未被 purgeHistory 清理(versionStatus != ARCHIVED)才能查到。
     */
    @Schema(title = "versionNo", description = "选填参数,指定查询某版本的影子;不传默认按设备当前绑定版本", example = "1900512345678901")
    private String versionNo;
}
