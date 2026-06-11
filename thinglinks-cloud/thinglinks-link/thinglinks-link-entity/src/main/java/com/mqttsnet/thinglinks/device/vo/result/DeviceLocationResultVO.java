package com.mqttsnet.thinglinks.device.vo.result;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * <p>
 * 表单查询方法返回值VO
 * 设备位置表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-05-30 23:05:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "DeviceLocationResultVO", description = "设备位置表")
public class DeviceLocationResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 设备标识
     */
    @Schema(description = "设备标识")
    private String deviceIdentification;
    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private BigDecimal latitude;
    /**
     * 经度
     */
    @Schema(description = "经度")
    private BigDecimal longitude;
    /**
     * 位置名称
     */
    @Schema(description = "位置名称")
    private String fullName;
    /**
     * 省,直辖市编码
     */
    @Schema(description = "省,直辖市编码")
    private String provinceCode;
    /**
     * 市编码
     */
    @Schema(description = "市编码")
    private String cityCode;
    /**
     * 区县
     */
    @Schema(description = "区县")
    private String regionCode;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


}
